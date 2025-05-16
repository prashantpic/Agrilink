package com.thesss.platform.common.security.encryption;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "common.encryption")
public class EncryptionProperties {

    private String aesKey;
    private String aesInitVector;
    private String algorithm = "AES/CBC/PKCS5PADDING";

    // Getters and Setters

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    public String getAesInitVector() {
        return aesInitVector;
    }

    public void setAesInitVector(String aesInitVector) {
        this.aesInitVector = aesInitVector;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}