package com.thesss.platform.gateway.dto;

import java.util.List;

/**
 * DTO for API key validation results.
 * Transports the result of an API key validation, including validity status and associated client details.
 */
public record ApiKeyValidationResponse(
    boolean isValid,
    String clientId,
    List<String> scopes,
    String errorMessage
) {
}