package com.thesss.platform.gateway.dto;

import java.util.List;

/**
 * DTO for token introspection results.
 * Transports the result of a token introspection, including active status and token claims.
 */
public record TokenIntrospectionResponse(
    boolean active,
    String sub,
    String iss,
    long exp,
    List<String> scope,
    String clientId
) {
}