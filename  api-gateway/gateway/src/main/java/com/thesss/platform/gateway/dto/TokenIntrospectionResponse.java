package com.thesss.platform.gateway.dto;

import java.util.List;

/**
 * DTO for token introspection results.
 * Transports the result of a token introspection, including active status and token claims.
 *
 * @param active    Indicates if the token is active.
 * @param sub       Subject of the token.
 * @param iss       Issuer of the token.
 * @param exp       Expiration time of the token (Unix timestamp).
 * @param scope     List of scopes associated with the token.
 * @param clientId  Client ID associated with the token.
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