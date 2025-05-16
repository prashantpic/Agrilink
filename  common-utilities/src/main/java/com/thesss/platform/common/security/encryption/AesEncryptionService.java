package com.thesss.platform.common.security.encryption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.spec.AlgorithmParameterSpec;
import java.security.GeneralSecurityException;

public class AesEncryptionService implements EncryptionService {

    private static final Logger log = LoggerFactory.getLogger(AesEncryptionService.class);

    private final SecretKeySpec secretKeySpec;
    private final IvParameterSpec ivParameterSpec; // May be null if algorithm doesn't require IV (e.g., ECB)
    private final String algorithmFullName; // Full algorithm string e.g., "AES/CBC/PKCS5PADDING"

    public AesEncryptionService(EncryptionProperties encryptionProperties) {
        this.algorithmFullName = encryptionProperties.getAlgorithm();

        if (encryptionProperties.getAesKey() == null || encryptionProperties.getAesKey().isEmpty()) {
            throw new IllegalArgumentException("AES encryption key (common.encryption.aes-key) cannot be null or empty.");
        }
        byte[] keyBytes = decodeKeyOrIv(encryptionProperties.getAesKey(), "AES key");
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) { // 128, 192, or 256 bits
            throw new IllegalArgumentException("Invalid AES key length: " + keyBytes.length * 8 + " bits. Must be 128, 192, or 256 bits.");
        }
        this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");

        // Initialize IV only if the algorithm requires it (e.g., CBC, GCM)
        // AES/ECB/... does not use an IV.
        if (algorithmFullName.toUpperCase().contains("/CBC/") || algorithmFullName.toUpperCase().contains("/GCM/")) {
            if (encryptionProperties.getAesInitVector() == null || encryptionProperties.getAesInitVector().isEmpty()) {
                throw new IllegalArgumentException("AES Initialization Vector (common.encryption.aes-init-vector) cannot be null or empty for algorithm: " + algorithmFullName);
            }
            byte[] ivBytes = decodeKeyOrIv(encryptionProperties.getAesInitVector(), "AES IV");
            // For CBC, IV is typically 16 bytes (128 bits). For GCM, 12 bytes (96 bits) is common.
            // This validation might need to be more flexible depending on exact algorithm support.
            if (algorithmFullName.toUpperCase().contains("/CBC/") && ivBytes.length != 16) {
                throw new IllegalArgumentException("Invalid AES IV length for CBC mode: " + ivBytes.length * 8 + " bits. Must be 128 bits (16 bytes).");
            }
            // Add GCM IV length check if strictly enforcing common lengths (e.g. 12 bytes)
            // else { log.warn("IV length for {} is {} bytes.", algorithmFullName, ivBytes.length); }
            this.ivParameterSpec = new IvParameterSpec(ivBytes);
        } else {
            this.ivParameterSpec = null; // Algorithm does not use an IV
        }
        log.info("AesEncryptionService initialized with algorithm: {}, Key Length: {} bits, IV Required: {}",
                 algorithmFullName, keyBytes.length * 8, (this.ivParameterSpec != null));
    }

    private byte[] decodeKeyOrIv(String encodedString, String type) {
        try {
            // Try Base64 first
            if (encodedString.matches("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$")) {
                 return Base64.getDecoder().decode(encodedString);
            }
            // Try Hex (simple check, could be more robust)
            if (encodedString.matches("^[0-9a-fA-F]+$") && encodedString.length() % 2 == 0) {
                return hexStringToByteArray(encodedString);
            }
            throw new IllegalArgumentException("Invalid encoding for " + type + ". Must be Base64 or Hex.");
        } catch (IllegalArgumentException e) {
            log.error("Failed to decode {}: {}", type, e.getMessage());
            throw new IllegalArgumentException("Failed to decode " + type + ": " + e.getMessage(), e);
        }
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


    @Override
    public String encrypt(String data) {
        if (data == null) { // Allow encrypting empty string if needed, but null input -> null output
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(algorithmFullName);
            if (ivParameterSpec != null) {
                 cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            } else {
                 cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            }
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (GeneralSecurityException e) {
            log.error("Encryption failed for algorithm {}: {}", algorithmFullName, e.getMessage(), e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(String encryptedData) {
        if (encryptedData == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(algorithmFullName);
             if (ivParameterSpec != null) {
                 cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
             } else {
                 cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
             }
            byte[] original = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(original);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) { // For Base64 decoding errors
            log.error("Decryption failed: Invalid Base64 input. {}", e.getMessage());
            throw new RuntimeException("Decryption failed due to invalid input format", e);
        } catch (GeneralSecurityException e) {
            log.error("Decryption failed for algorithm {}: {}", algorithmFullName, e.getMessage(), e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
}