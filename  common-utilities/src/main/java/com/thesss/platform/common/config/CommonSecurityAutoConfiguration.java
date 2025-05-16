package com.thesss.platform.common.config;

import com.thesss.platform.common.security.config.CommonSecurityProperties;
import com.thesss.platform.common.security.encryption.AesEncryptionService;
import com.thesss.platform.common.security.encryption.EncryptionProperties;
import com.thesss.platform.common.security.encryption.EncryptionService;
import com.thesss.platform.common.security.hashing.DefaultPasswordEncoderProvider;
import com.thesss.platform.common.security.hashing.PasswordEncoderProvider;
import com.thesss.platform.common.security.jwt.JwtProperties;
import com.thesss.platform.common.security.jwt.JwtTokenProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, CommonSecurityProperties.class, EncryptionProperties.class})
public class CommonSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoderProvider passwordEncoderProvider(CommonSecurityProperties commonSecurityProperties) {
        return new DefaultPasswordEncoderProvider(commonSecurityProperties);
    }

    // Expose Spring Security's PasswordEncoder bean for direct use if needed
    // This makes it easier for consuming applications to inject PasswordEncoder directly
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder springPasswordEncoder(PasswordEncoderProvider passwordEncoderProvider) {
        return passwordEncoderProvider.getEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtTokenProvider jwtTokenProvider(JwtProperties jwtProperties) {
        return new JwtTokenProvider(jwtProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public EncryptionService encryptionService(EncryptionProperties encryptionProperties) {
        return new AesEncryptionService(encryptionProperties);
    }
}