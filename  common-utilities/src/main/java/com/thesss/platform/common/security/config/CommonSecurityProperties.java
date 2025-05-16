package com.thesss.platform.common.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "common.security")
public class CommonSecurityProperties {

    private List<String> allowedOrigins = new ArrayList<>();
    private String passwordEncoderDefault = "argon2id";

    // Getters and Setters

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public String getPasswordEncoderDefault() {
        return passwordEncoderDefault;
    }

    public void setPasswordEncoderDefault(String passwordEncoderDefault) {
        this.passwordEncoderDefault = passwordEncoderDefault;
    }
}