package com.example.studentmanagement.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

public class SecurityUtilTest {

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<SecurityUtil> constructor = SecurityUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        SecurityUtil instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    void testGenerateRandomToken() {
        String tokenDefault = SecurityUtil.generateRandomToken();
        assertNotNull(tokenDefault);
        assertFalse(tokenDefault.isEmpty());

        String tokenCustom = SecurityUtil.generateRandomToken(16);
        assertNotNull(tokenCustom);

        String tokenZero = SecurityUtil.generateRandomToken(0);
        assertNotNull(tokenZero);
    }

    @Test
    void testGenerateSalt() {
        String saltDefault = SecurityUtil.generateSalt(16);
        assertNotNull(saltDefault);

        String saltNegative = SecurityUtil.generateSalt(-5);
        assertNotNull(saltNegative);
    }

    @Test
    void testHashWithSalt() {
        String hash1 = SecurityUtil.hashWithSalt("password123", "saltXYZ");
        assertNotNull(hash1);

        String hash2 = SecurityUtil.hashWithSalt("password123", "saltXYZ");
        assertEquals(hash1, hash2);

        String hash3 = SecurityUtil.hashWithSalt("password123", "saltABC");
        assertNotEquals(hash1, hash3);

        // algorithm variations
        String hashSha512 = SecurityUtil.hashWithSalt("password123", "saltXYZ", "SHA-512");
        assertNotNull(hashSha512);

        String hashBlankAlgo = SecurityUtil.hashWithSalt("password123", "saltXYZ", "   ");
        assertEquals(hash1, hashBlankAlgo);

        String hashNullSalt = SecurityUtil.hashWithSalt("password123", null, null);
        assertNotNull(hashNullSalt);

        assertNull(SecurityUtil.hashWithSalt(null, "salt"));

        assertThrows(IllegalArgumentException.class, () -> SecurityUtil.hashWithSalt("pass", "salt", "INVALID_ALGORITHM"));
    }

    @Test
    void testConstantTimeEquals() {
        assertTrue(SecurityUtil.constantTimeEquals(null, null));
        assertFalse(SecurityUtil.constantTimeEquals("secret", null));
        assertFalse(SecurityUtil.constantTimeEquals(null, "secret"));
        assertTrue(SecurityUtil.constantTimeEquals("superSecretToken", "superSecretToken"));
        assertFalse(SecurityUtil.constantTimeEquals("superSecretToken", "wrongToken"));
    }

    @Test
    void testIsPasswordStrong() {
        assertFalse(SecurityUtil.isPasswordStrong(null));
        assertFalse(SecurityUtil.isPasswordStrong(""));
        assertFalse(SecurityUtil.isPasswordStrong("   "));

        // too short (< 8)
        assertFalse(SecurityUtil.isPasswordStrong("A1@b"));

        // no uppercase
        assertFalse(SecurityUtil.isPasswordStrong("admin@12345"));

        // no lowercase
        assertFalse(SecurityUtil.isPasswordStrong("ADMIN@12345"));

        // no digit
        assertFalse(SecurityUtil.isPasswordStrong("Admin@Special"));

        // no special char
        assertFalse(SecurityUtil.isPasswordStrong("Admin1234567"));

        // strong password
        assertTrue(SecurityUtil.isPasswordStrong("Admin@12345"));
        assertTrue(SecurityUtil.isPasswordStrong("P@ssw0rd2026!"));
    }

    @Test
    void testMaskSensitiveData() {
        assertNull(SecurityUtil.maskSensitiveData(null, 2, 2));
        assertEquals("", SecurityUtil.maskSensitiveData("", 2, 2));

        // start + end >= length
        assertEquals("short", SecurityUtil.maskSensitiveData("short", 3, 3));

        // normal masking
        assertEquals("us****me", SecurityUtil.maskSensitiveData("username", 2, 2));
        assertEquals("ad*********or", SecurityUtil.maskSensitiveData("administrator", 2, 2));

        String masked = SecurityUtil.maskSensitiveData("abcdefghij", 2, 3);
        assertEquals("ab*****hij", masked);
    }
}
