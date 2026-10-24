package com.example.studentmanagement.util;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Utility class for AES (Advanced Encryption Standard) encryption and decryption.
 * Based on Baeldung's Java AES Encryption and Decryption guide:
 * https://www.baeldung.com/java-aes-encryption-decryption
 */
public final class AESUtil {

    public static final String DEFAULT_ALGORITHM = "AES/CBC/PKCS5Padding";

    private AESUtil() {
        // Prevent instantiation
    }

    /**
     * Generate a new AES SecretKey with the given bit size (128, 192, or 256).
     */
    public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n);
        return keyGenerator.generateKey();
    }

    /**
     * Generate a SecretKey from a password and salt using PBKDF2WithHmacSHA256.
     */
    public static SecretKey getKeyFromPassword(String password, String salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    /**
     * Generate a random 16-byte Initialization Vector (IV).
     */
    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    /**
     * Encrypt a plain text string using the specified algorithm, SecretKey, and IV.
     * Returns Base64-encoded ciphertext.
     */
    public static String encrypt(String algorithm, String input, SecretKey key, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        if (input == null) {
            return null;
        }
        String algo = (algorithm != null && !algorithm.trim().isEmpty()) ? algorithm : DEFAULT_ALGORITHM;
        Cipher cipher = Cipher.getInstance(algo);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        byte[] cipherText = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(cipherText);
    }

    /**
     * Decrypt a Base64-encoded ciphertext string using the specified algorithm, SecretKey, and IV.
     */
    public static String decrypt(String algorithm, String cipherText, SecretKey key, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        if (cipherText == null) {
            return null;
        }
        String algo = (algorithm != null && !algorithm.trim().isEmpty()) ? algorithm : DEFAULT_ALGORITHM;
        Cipher cipher = Cipher.getInstance(algo);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }

    /**
     * Encrypt using default algorithm AES/CBC/PKCS5Padding.
     */
    public static String encrypt(String input, SecretKey key, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        return encrypt(DEFAULT_ALGORITHM, input, key, iv);
    }

    /**
     * Decrypt using default algorithm AES/CBC/PKCS5Padding.
     */
    public static String decrypt(String cipherText, SecretKey key, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {
        return decrypt(DEFAULT_ALGORITHM, cipherText, key, iv);
    }

    /**
     * Encrypt a file and write encrypted content to the output file using CipherOutputStream.
     */
    public static void encryptFile(String algorithm, SecretKey key, IvParameterSpec iv,
                                   File inputFile, File outputFile)
            throws IOException, NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException {
        String algo = (algorithm != null && !algorithm.trim().isEmpty()) ? algorithm : DEFAULT_ALGORITHM;
        Cipher cipher = Cipher.getInstance(algo);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        try (FileInputStream inputStream = new FileInputStream(inputFile);
             FileOutputStream outputStream = new FileOutputStream(outputFile);
             CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, cipher)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                cipherOutputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Decrypt an encrypted file and write plain content to the output file using CipherInputStream.
     */
    public static void decryptFile(String algorithm, SecretKey key, IvParameterSpec iv,
                                   File inputFile, File outputFile)
            throws IOException, NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException {
        String algo = (algorithm != null && !algorithm.trim().isEmpty()) ? algorithm : DEFAULT_ALGORITHM;
        Cipher cipher = Cipher.getInstance(algo);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        try (FileInputStream inputStream = new FileInputStream(inputFile);
             CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = cipherInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Encrypt a Serializable object into a SealedObject.
     */
    public static SealedObject encryptObject(String algorithm, Serializable object,
                                             SecretKey key, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            IOException, IllegalBlockSizeException {
        String algo = (algorithm != null && !algorithm.trim().isEmpty()) ? algorithm : DEFAULT_ALGORITHM;
        Cipher cipher = Cipher.getInstance(algo);
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);
        return new SealedObject(object, cipher);
    }

    /**
     * Decrypt a SealedObject back into its original Serializable object.
     */
    public static Serializable decryptObject(String algorithm, SealedObject sealedObject,
                                             SecretKey key, IvParameterSpec iv)
            throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            ClassNotFoundException, BadPaddingException, IllegalBlockSizeException, IOException {
        String algo = (algorithm != null && !algorithm.trim().isEmpty()) ? algorithm : DEFAULT_ALGORITHM;
        Cipher cipher = Cipher.getInstance(algo);
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return (Serializable) sealedObject.getObject(cipher);
    }

    /**
     * Encrypt plain text using a password, auto-generating key and IV,
     * and returns a composite base64 string formatted as: salt:iv:ciphertext.
     */
    public static String encryptWithPassword(String input, String password, String salt)
            throws Exception {
        if (input == null) return null;
        SecretKey key = getKeyFromPassword(password, salt);
        IvParameterSpec iv = generateIv();
        String cipherText = encrypt(DEFAULT_ALGORITHM, input, key, iv);
        String ivBase64 = Base64.getEncoder().encodeToString(iv.getIV());
        String saltBase64 = Base64.getEncoder().encodeToString(salt.getBytes());
        return saltBase64 + ":" + ivBase64 + ":" + cipherText;
    }

    /**
     * Decrypt a composite formatted string (salt:iv:ciphertext) using password.
     */
    public static String decryptWithPassword(String compositeCipherText, String password)
            throws Exception {
        if (compositeCipherText == null) return null;
        String[] parts = compositeCipherText.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid encrypted format. Expected salt:iv:ciphertext");
        }
        String salt = new String(Base64.getDecoder().decode(parts[0]));
        byte[] ivBytes = Base64.getDecoder().decode(parts[1]);
        String cipherText = parts[2];

        SecretKey key = getKeyFromPassword(password, salt);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        return decrypt(DEFAULT_ALGORITHM, cipherText, key, iv);
    }
}
