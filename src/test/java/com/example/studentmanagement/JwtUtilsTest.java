package com.example.studentmanagement;

import com.example.studentmanagement.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret",
                "testEducazeSecretKey1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$%^&*()");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000);
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtUtils.generateTokenFromUsername("testuser", List.of("ROLE_ADMIN", "ROLE_LECTURER"));
        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));

        String username = jwtUtils.getUserNameFromJwtToken(token);
        assertEquals("testuser", username);

        List<String> roles = jwtUtils.getRolesFromJwtToken(token);
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_ADMIN"));
        assertTrue(roles.contains("ROLE_LECTURER"));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtUtils.validateJwtToken("invalid-token-string"));
    }

    @Test
    void testExpiredToken() {
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", -1000);
        String expiredToken = jwtUtils.generateTokenFromUsername("expireduser", List.of("ROLE_STUDENT"));
        assertFalse(jwtUtils.validateJwtToken(expiredToken));
    }
}
