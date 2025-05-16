package com.thesss.platform.farmer.config;

import com.thesss.platform.farmer.domain.service.EncryptionService;
import com.thesss.platform.farmer.infrastructure.security.AesEncryptionServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptionConfig {

    @Value("${app.encryption.aes.key}")
    private String aesKey;

    @Value("${app.encryption.aes.iv-spec}")
    private String aesIvSpec; // This might be a Base64 encoded IV or a strategy identifier

    @Bean
    public EncryptionService encryptionService() {
        // Consider if IV should be generated per encryption or if a static IV is used (less secure)
        // The AesEncryptionServiceImpl should handle the IV strategy based on this ivSpec
        return new AesEncryptionServiceImpl(aesKey, aesIvSpec);
    }
}