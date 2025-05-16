package com.thesss.platform.farmer.infrastructure.security;

import com.thesss.platform.farmer.domain.model.EncryptedValue; // Assuming domain model exists
import com.thesss.platform.farmer.domain.service.EncryptionService;
import com.thesss.platform.farmer.exception.DataEncryptionException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// No @Service annotation here as it's instantiated via EncryptionConfig @Bean
// If it were to be component-scanned, @Service would be appropriate.

public class AesEncryptionServiceImpl implements EncryptionService {

    private static final Logger log = LoggerFactory.getLogger(AesEncryptionServiceImpl.class);
    private static final String ALGORITHM = "AES";
    private static final String AES_CIPHER_ALGORITHM = "AES/GCM/NoPadding"; // GCM is recommended for AEAD
    private static final int GCM_IV_LENGTH = 12; // 96 bits is recommended for GCM IV
    private static final int GCM_TAG_LENGTH = 128; // In bits

    private final SecretKey secretKey;
    // private final byte[] staticIv; // Static IV is less secure, prefer generating IV per encryption

    public AesEncryptionServiceImpl(String base64Key, String base64IvSpecOrStrategy) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
                throw new IllegalArgumentException("Invalid AES key length. Must be 16, 24, or 32 bytes.");
            }
            this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            // If base64IvSpecOrStrategy is a static IV (less secure, mainly for specific compatibility)
            // this.staticIv = Base64.getDecoder().decode(base64IvSpecOrStrategy);
            // if (this.staticIv.length != GCM_IV_LENGTH) {
            //     throw new IllegalArgumentException("Static IV length must be " + GCM_IV_LENGTH + " bytes for GCM.");
            // }
            // For GCM, IV should ideally be unique per encryption. We'll generate it in encrypt().
        } catch (IllegalArgumentException e) {
            log.error("Error initializing AES Encryption Service: {}", e.getMessage());
            throw new DataEncryptionException("Failed to initialize encryption service: " + e.getMessage(), e);
        }
    }

    @Override
    public EncryptedValue encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = SecureRandom.getInstanceStrong(); // Or SecureRandom.getInstance("SHA1PRNG")
            random.nextBytes(iv);

            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Prepend IV to the ciphertext for storage. This is a common practice.
            byte[] ivAndCiphertext = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, ivAndCiphertext, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, ivAndCiphertext, iv.length, encryptedBytes.length);

            return EncryptedValue.of(Base64.getEncoder().encodeToString(ivAndCiphertext));
        } catch (NoSuchAlgorithmException e) {
            log.error("Strong SecureRandom algorithm not available.", e);
            // Fallback or rethrow as critical error
            throw new DataEncryptionException("Encryption failed due to missing strong SecureRandom.", e);
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new DataEncryptionException("Encryption failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String decrypt(EncryptedValue encryptedValue) {
        if (encryptedValue == null || encryptedValue.getValue() == null) {
            return null;
        }
        try {
            byte[] ivAndCiphertext = Base64.getDecoder().decode(encryptedValue.getValue());

            if (ivAndCiphertext.length < GCM_IV_LENGTH) {
                throw new DataEncryptionException("Invalid encrypted data: too short to contain IV.");
            }

            // Extract IV from the beginning of the combined array
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(ivAndCiphertext, 0, iv, 0, iv.length);

            // Extract ciphertext
            byte[] ciphertext = new byte[ivAndCiphertext.length - iv.length];
            System.arraycopy(ivAndCiphertext, iv.length, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

            byte[] decryptedBytes = cipher.doFinal(ciphertext);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed for value (actual value omitted for security): {}", e.getMessage());
            // It's crucial not to log the encryptedValue.getValue() directly here if it contains sensitive data,
            // unless it's in a secure, access-controlled debug log.
            throw new DataEncryptionException("Decryption failed: " + e.getMessage(), e);
        }
    }
}