package com.example.studentmanagement;

import com.example.studentmanagement.entity.RoleType;
import com.example.studentmanagement.entity.User;
import com.example.studentmanagement.repository.UserRepository;
import com.example.studentmanagement.security.service.UserDetailsImpl;
import com.example.studentmanagement.security.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void testLoadUserByUsername_Success() {
        User user = new User("john_doe", "john@example.com", "secret", "John Doe");
        user.setId(5L);
        user.setRoles(Set.of(RoleType.ROLE_STUDENT, RoleType.ROLE_LECTURER));

        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("john_doe");
        assertNotNull(details);
        assertEquals("john_doe", details.getUsername());
        assertEquals("secret", details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertEquals(2, details.getAuthorities().size());

        UserDetailsImpl userDetails = (UserDetailsImpl) details;
        assertEquals(5L, userDetails.getId());
        assertEquals("john@example.com", userDetails.getEmail());
        assertEquals("John Doe", userDetails.getFullName());

        // Equals check
        UserDetailsImpl same = (UserDetailsImpl) UserDetailsImpl.build(user);
        assertEquals(userDetails, userDetails);
        assertEquals(userDetails, same);
        assertNotEquals(userDetails, null);
        assertNotEquals(userDetails, new Object());

        User otherUser = new User("other", "other@example.com", "pass", "Other");
        otherUser.setId(6L);
        otherUser.setRoles(Set.of());
        UserDetailsImpl different = (UserDetailsImpl) UserDetailsImpl.build(otherUser);
        assertNotEquals(userDetails, different);
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("unknown"));
    }
}
