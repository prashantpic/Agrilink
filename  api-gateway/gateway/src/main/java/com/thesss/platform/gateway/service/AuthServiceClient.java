package com.thesss.platform.gateway.service;

import com.thesss.platform.gateway.dto.ApiKeyValidationResponse;
import com.thesss.platform.gateway.dto.TokenIntrospectionResponse;
import reactor.core.publisher.Mono;

public interface AuthServiceClient {

    /**
     * Validates an API key against the authentication service.
     *
     * @param apiKey The API key to validate.
     * @return A Mono emitting {@link ApiKeyValidationResponse} containing validation status and details.
     */
    Mono<ApiKeyValidationResponse> validateApiKey(String apiKey);

    /**
     * Introspects a token (e.g., opaque token) against the authentication service.
     * Useful for token types not directly handled by Spring Security's OAuth2 resource server.
     *
     * @param token The token to introspect.
     * @return A Mono emitting {@link TokenIntrospectionResponse} containing token details.
     */
    Mono<TokenIntrospectionResponse> introspectToken(String token);
}