package com.thesss.platform.common.security.encryption;

public interface EncryptionService {

    String encrypt(String data);

    String decrypt(String encryptedData);
}