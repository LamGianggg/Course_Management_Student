package com.example.studentmanagement.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class AESUtilTest {

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<AESUtil> constructor = AESUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        AESUtil instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    void testKeyGeneration() throws Exception {
        SecretKey key128 = AESUtil.generateKey(128);
        assertNotNull(key128);
        assertEquals("AES", key128.getAlgorithm());

        SecretKey key256 = AESUtil.generateKey(256);
        assertNotNull(key256);

        SecretKey pbeKey = AESUtil.getKeyFromPassword("mySuperSecretPassword", "someSaltValue");
        assertNotNull(pbeKey);
        assertEquals("AES", pbeKey.getAlgorithm());
    }

    @Test
    void testIvGeneration() {
        IvParameterSpec iv = AESUtil.generateIv();
        assertNotNull(iv);
        assertEquals(16, iv.getIV().length);
    }

    @Test
    void testStringEncryptionDecryption_WithDefaultAlgorithm() throws Exception {
        SecretKey key = AESUtil.generateKey(128);
        IvParameterSpec iv = AESUtil.generateIv();
        String original = "Hello Baeldung Java AES!";

        String cipherText = AESUtil.encrypt(original, key, iv);
        assertNotNull(cipherText);
        assertNotEquals(original, cipherText);

        String decrypted = AESUtil.decrypt(cipherText, key, iv);
        assertEquals(original, decrypted);
    }

    @Test
    void testStringEncryptionDecryption_WithCustomAndBlankAlgorithm() throws Exception {
        SecretKey key = AESUtil.generateKey(128);
        IvParameterSpec iv = AESUtil.generateIv();
        String original = "Test algorithm branches";

        // explicit algorithm
        String cipher1 = AESUtil.encrypt("AES/CBC/PKCS5Padding", original, key, iv);
        String dec1 = AESUtil.decrypt("AES/CBC/PKCS5Padding", cipher1, key, iv);
        assertEquals(original, dec1);

        // null/blank algorithm falls back to DEFAULT_ALGORITHM
        String cipher2 = AESUtil.encrypt("   ", original, key, iv);
        String dec2 = AESUtil.decrypt("", cipher2, key, iv);
        assertEquals(original, dec2);

        String cipher3 = AESUtil.encrypt((String) null, original, key, iv);
        String dec3 = AESUtil.decrypt((String) null, cipher3, key, iv);
        assertEquals(original, dec3);
    }

    @Test
    void testStringEncryptionDecryption_NullInputs() throws Exception {
        SecretKey key = AESUtil.generateKey(128);
        IvParameterSpec iv = AESUtil.generateIv();

        assertNull(AESUtil.encrypt(null, key, iv));
        assertNull(AESUtil.decrypt(null, key, iv));
        assertNull(AESUtil.encrypt(AESUtil.DEFAULT_ALGORITHM, null, key, iv));
        assertNull(AESUtil.decrypt(AESUtil.DEFAULT_ALGORITHM, null, key, iv));
    }

    @Test
    void testFileEncryptionDecryption(@TempDir Path tempDir) throws Exception {
        SecretKey key = AESUtil.generateKey(128);
        IvParameterSpec iv = AESUtil.generateIv();

        Path inputPath = tempDir.resolve("input.txt");
        Path encryptedPath = tempDir.resolve("encrypted.enc");
        Path decryptedPath = tempDir.resolve("decrypted.txt");

        String fileContent = "This is confidential file content meant for AES file encryption tests.";
        Files.writeString(inputPath, fileContent);

        // explicit algorithm
        AESUtil.encryptFile("AES/CBC/PKCS5Padding", key, iv, inputPath.toFile(), encryptedPath.toFile());
        assertTrue(Files.exists(encryptedPath));
        assertTrue(Files.size(encryptedPath) > 0);

        AESUtil.decryptFile("AES/CBC/PKCS5Padding", key, iv, encryptedPath.toFile(), decryptedPath.toFile());
        assertTrue(Files.exists(decryptedPath));
        assertEquals(fileContent, Files.readString(decryptedPath));

        // null/blank algorithm fallback
        Path encryptedPath2 = tempDir.resolve("encrypted2.enc");
        Path decryptedPath2 = tempDir.resolve("decrypted2.txt");

        AESUtil.encryptFile(null, key, iv, inputPath.toFile(), encryptedPath2.toFile());
        AESUtil.decryptFile("", key, iv, encryptedPath2.toFile(), decryptedPath2.toFile());
        assertEquals(fileContent, Files.readString(decryptedPath2));
    }

    @Test
    void testObjectEncryptionDecryption() throws Exception {
        SecretKey key = AESUtil.generateKey(128);
        IvParameterSpec iv = AESUtil.generateIv();

        String payload = "Serializable Payload String";

        // explicit algorithm
        SealedObject sealedObject = AESUtil.encryptObject("AES/CBC/PKCS5Padding", payload, key, iv);
        assertNotNull(sealedObject);

        String decryptedPayload = (String) AESUtil.decryptObject("AES/CBC/PKCS5Padding", sealedObject, key, iv);
        assertEquals(payload, decryptedPayload);

        // null/blank algorithm fallback
        SealedObject sealedObject2 = AESUtil.encryptObject(null, payload, key, iv);
        String dec2 = (String) AESUtil.decryptObject("", sealedObject2, key, iv);
        assertEquals(payload, dec2);
    }

    @Test
    void testPasswordBasedCompositeEncryptionDecryption() throws Exception {
        String plain = "User sensitive password data!";
        String password = "SecretMasterPassword123";
        String salt = "saltValueForTest";

        String composite = AESUtil.encryptWithPassword(plain, password, salt);
        assertNotNull(composite);
        assertTrue(composite.contains(":"));

        String decrypted = AESUtil.decryptWithPassword(composite, password);
        assertEquals(plain, decrypted);

        // null input
        assertNull(AESUtil.encryptWithPassword(null, password, salt));
        assertNull(AESUtil.decryptWithPassword(null, password));

        // invalid format
        assertThrows(IllegalArgumentException.class, () -> AESUtil.decryptWithPassword("invalidFormatString", password));
        assertThrows(IllegalArgumentException.class, () -> AESUtil.decryptWithPassword("part1:part2", password));
    }
}
