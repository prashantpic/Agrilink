package com.thesss.platform.farmer.domain.service;

import com.thesss.platform.farmer.domain.model.EncryptedValue;

/**
 * Interface for encryption and decryption services within the domain.
 * REQ-FRM-010, REQ-FRM-011
 */
public interface EncryptionService {

    /**
     * Encrypts the given plaintext string.
     * @param plaintext The string to encrypt.
     * @return An {@link EncryptedValue} containing the encrypted data.
     * @throws com.thesss.platform.farmer.exception.DataEncryptionException if encryption fails.
     */
    EncryptedValue encrypt(String plaintext);

    /**
     * Decrypts the given {@link EncryptedValue}.
     * @param encryptedValue The encrypted data to decrypt.
     * @return The original plaintext string.
     * @throws com.thesss.platform.farmer.exception.DataEncryptionException if decryption fails.
     */
    String decrypt(EncryptedValue encryptedValue);
}