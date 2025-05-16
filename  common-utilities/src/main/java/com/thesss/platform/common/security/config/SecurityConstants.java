package com.thesss.platform.common.security.config;

public final class SecurityConstants {

    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-ID";

    private SecurityConstants() {
        // restrict instantiation
    }
}