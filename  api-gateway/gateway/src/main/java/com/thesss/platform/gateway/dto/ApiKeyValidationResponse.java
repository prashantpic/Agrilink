package com.thesss.platform.gateway.dto;

import java.util.List;

/**
 * DTO for API key validation results.
 * Transports the result of an API key validation, including validity status and associated client details.
 *
 * @param isValid       Indicates if the API key is valid.
 * @param clientId      The client ID associated with the API key.
 * @param scopes        A list of scopes granted to the API key.
 * @param errorMessage  An error message if validation failed.
 */
public record ApiKeyValidationResponse(
    boolean isValid,
    String clientId,
    List<String> scopes,
    String errorMessage
) {
}