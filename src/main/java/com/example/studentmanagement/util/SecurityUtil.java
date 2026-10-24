package com.example.studentmanagement.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Utility class for common security operations:
 * - Secure random token and salt generation
 * - Cryptographic hashing (SHA-256 / SHA-512)
 * - Timing-attack resistant string comparison
 * - Password strength checking
 * - Sensitive data masking
 */
public final class SecurityUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String DEFAULT_HASH_ALGORITHM = "SHA-256";

    // At least 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special character
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$"
    );

    private SecurityUtil() {
        // Prevent instantiation
    }

    /**
     * Generate a cryptographically secure random token (URL-safe Base64).
     */
    public static String generateRandomToken(int byteLength) {
        int length = byteLength > 0 ? byteLength : 32;
        byte[] randomBytes = new byte[length];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Generate a default 32-byte secure random token.
     */
    public static String generateRandomToken() {
        return generateRandomToken(32);
    }

    /**
     * Generate a random hex salt string.
     */
    public static String generateSalt(int byteLength) {
        int length = byteLength > 0 ? byteLength : 16;
        byte[] salt = new byte[length];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hash input string with salt and algorithm.
     */
    public static String hashWithSalt(String input, String salt, String algorithm) {
        if (input == null) {
            return null;
        }
        String safeSalt = salt != null ? salt : "";
        String algo = (algorithm != null && !algorithm.trim().isEmpty()) ? algorithm : DEFAULT_HASH_ALGORITHM;

        try {
            MessageDigest digest = MessageDigest.getInstance(algo);
            digest.update(safeSalt.getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unsupported hashing algorithm: " + algo, e);
        }
    }

    /**
     * Hash input string with salt using default SHA-256.
     */
    public static String hashWithSalt(String input, String salt) {
        return hashWithSalt(input, salt, DEFAULT_HASH_ALGORITHM);
    }

    /**
     * Constant-time string equality check to protect against timing attacks.
     */
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Check if a password meets minimum strong complexity rules.
     */
    public static boolean isPasswordStrong(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Mask sensitive strings (e.g. tokens, personal data) leaving start and end visible.
     */
    public static String maskSensitiveData(String data, int visibleStart, int visibleEnd) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        int len = data.length();
        int start = Math.max(0, visibleStart);
        int end = Math.max(0, visibleEnd);

        if (start + end >= len) {
            return data;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(data, 0, start);
        sb.append("*".repeat(len - start - end));
        sb.append(data, len - end, len);
        return sb.toString();
    }
}
