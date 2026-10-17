package com.example.studentmanagement;

import com.example.studentmanagement.dto.LoginRequest;
import com.example.studentmanagement.dto.RegisterRequest;
import com.example.studentmanagement.entity.RoleType;
import com.example.studentmanagement.entity.User;
import com.example.studentmanagement.repository.UserRepository;
import com.example.studentmanagement.security.jwt.JwtUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RoleBasedAccessControlTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private String adminToken;
    private String lecturerToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        // Ensure test users exist with known passwords
        createOrUpdateUser("testadmin", "testadmin@educaze.com", "admin123", Set.of(RoleType.ROLE_ADMIN));
        createOrUpdateUser("testlecturer", "testlecturer@educaze.com", "lecturer123", Set.of(RoleType.ROLE_LECTURER));
        createOrUpdateUser("teststudent", "teststudent@educaze.com", "student123", Set.of(RoleType.ROLE_STUDENT));

        adminToken = jwtUtils.generateTokenFromUsername("testadmin", java.util.List.of("ROLE_ADMIN"));
        lecturerToken = jwtUtils.generateTokenFromUsername("testlecturer", java.util.List.of("ROLE_LECTURER"));
        studentToken = jwtUtils.generateTokenFromUsername("teststudent", java.util.List.of("ROLE_STUDENT"));
    }

    private void createOrUpdateUser(String username, String email, String password, Set<RoleType> roles) {
        User user = userRepository.findByUsername(username).orElse(new User());
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Test
    void testLoginSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testadmin", "admin123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.username").value("testadmin"))
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(responseStr);
        String token = root.path("data").path("token").asText();
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    void testLoginFailureWrongPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testadmin", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRegisterUser() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "newstudent", "newstudent@educaze.com", "password123", "New Student", Set.of("STUDENT")
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("newstudent"));
    }

    @Test
    void testRegisterDuplicateUsernameFails() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "testadmin", "unique_email@educaze.com", "password123", "Dup Admin", Set.of("ADMIN")
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAnonymousAccessToPublicStudentFilter() throws Exception {
        mockMvc.perform(get("/api/students/filter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testAnonymousAccessToAdminEndpointFails() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testStudentAccessToAdminEndpointForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testLecturerAccessToAdminEndpointForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminAccessToAdminEndpointSuccess() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testStudentCannotDeleteDepartment() throws Exception {
        mockMvc.perform(delete("/api/departments/999")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testLecturerCannotDeleteDepartment() throws Exception {
        mockMvc.perform(delete("/api/departments/999")
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetCurrentUserProfile() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("testadmin"));
    }
}
