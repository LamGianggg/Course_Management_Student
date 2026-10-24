package com.example.studentmanagement;

import com.example.studentmanagement.dto.ApiResponse;
import com.example.studentmanagement.dto.UserDto;
import com.example.studentmanagement.entity.RoleType;
import com.example.studentmanagement.entity.User;
import com.example.studentmanagement.repository.UserRepository;
import com.example.studentmanagement.rest.AuthController;
import com.example.studentmanagement.security.jwt.JwtUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthController authController;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetCurrentUser_AuthNull() {
        SecurityContextHolder.clearContext();
        ResponseEntity<ApiResponse<UserDto>> response = authController.getCurrentUser();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetCurrentUser_NotAuthenticated() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(auth);

        ResponseEntity<ApiResponse<UserDto>> response = authController.getCurrentUser();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetCurrentUser_AnonymousPrincipal() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("anonymousUser");
        SecurityContextHolder.getContext().setAuthentication(auth);

        ResponseEntity<ApiResponse<UserDto>> response = authController.getCurrentUser();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetCurrentUser_UserNotFoundInDb() {
        Authentication auth = new UsernamePasswordAuthenticationToken("ghost", "pass", java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        ResponseEntity<ApiResponse<UserDto>> response = authController.getCurrentUser();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetCurrentUser_Success() {
        Authentication auth = new UsernamePasswordAuthenticationToken("alice", "pass", java.util.Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = new User("alice", "alice@example.com", "pass", "Alice Wonder");
        user.setId(1L);
        user.setRoles(Set.of(RoleType.ROLE_STUDENT));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        ResponseEntity<ApiResponse<UserDto>> response = authController.getCurrentUser();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("alice", response.getBody().getData().getUsername());
    }
}
