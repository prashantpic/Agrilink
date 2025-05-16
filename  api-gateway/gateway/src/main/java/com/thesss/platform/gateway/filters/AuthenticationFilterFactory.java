package com.thesss.platform.gateway.filters;

import com.thesss.platform.gateway.dto.ApiKeyValidationResponse;
import com.thesss.platform.gateway.service.AuthServiceClient;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class AuthenticationFilterFactory extends AbstractGatewayFilterFactory<AuthenticationFilterFactory.Config> {

    private static final String API_KEY_HEADER_NAME_DEFAULT = "X-API-KEY";
    public static final String AUTHENTICATED_CLIENT_ID_ATTR = "authenticatedClientId";
    public static final String AUTHENTICATED_CLIENT_SCOPES_ATTR = "authenticatedClientScopes";

    private final AuthServiceClient authServiceClient;

    @Value("${gateway.auth.api-key.header-name:" + API_KEY_HEADER_NAME_DEFAULT + "}")
    private String apiKeyHeaderName;

    @Value("${gateway.auth.use-remote-auth-service-for-api-keys:true}")
    private boolean useRemoteAuthServiceForApiKeys;

    @Autowired
    public AuthenticationFilterFactory(AuthServiceClient authServiceClient) {
        super(Config.class);
        this.authServiceClient = authServiceClient;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            if (!request.getHeaders().containsKey(apiKeyHeaderName)) {
                log.warn("Missing API Key header: {}", apiKeyHeaderName);
                return unauthorizedResponse(exchange, "Missing API Key");
            }

            String apiKey = request.getHeaders().getFirst(apiKeyHeaderName);
            if (apiKey == null || apiKey.isBlank()) {
                log.warn("Blank API Key provided in header: {}", apiKeyHeaderName);
                return unauthorizedResponse(exchange, "Invalid API Key format");
            }

            if (useRemoteAuthServiceForApiKeys) {
                return authServiceClient.validateApiKey(apiKey)
                    .flatMap(validationResponse -> {
                        if (validationResponse.isValid()) {
                            log.debug("API Key validated successfully for client: {}", validationResponse.getClientId());
                            exchange.getAttributes().put(AUTHENTICATED_CLIENT_ID_ATTR, validationResponse.getClientId());
                            exchange.getAttributes().put(AUTHENTICATED_CLIENT_SCOPES_ATTR, validationResponse.getScopes());
                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header("X-Authenticated-Client-Id", validationResponse.getClientId())
                                .build();
                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        } else {
                            log.warn("API Key validation failed: {}", validationResponse.getErrorMessage());
                            return unauthorizedResponse(exchange, "Invalid API Key: " + validationResponse.getErrorMessage());
                        }
                    })
                    .onErrorResume(throwable -> {
                        log.error("Error during API Key validation call", throwable);
                        return errorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "API Key validation service error");
                    });
            } else {
                // Placeholder for local/simple API key validation if useRemoteAuthServiceForApiKeys is false
                log.warn("Remote auth service for API keys is disabled. API key validation skipped/simplified.");
                // Example: a very basic check. Replace with actual local validation if needed.
                if ("dummy-api-key".equals(apiKey)) {
                     exchange.getAttributes().put(AUTHENTICATED_CLIENT_ID_ATTR, "local-dummy-client");
                     exchange.getAttributes().put(AUTHENTICATED_CLIENT_SCOPES_ATTR, Collections.singletonList("read"));
                     return chain.filter(exchange);
                }
                return unauthorizedResponse(exchange, "Invalid API Key (local check failed)");
            }
        };
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        return errorResponse(exchange, HttpStatus.UNAUTHORIZED, message);
    }

    private Mono<Void> errorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String errorBody = String.format("{\"timestamp\":%d,\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                System.currentTimeMillis(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value());
        DataBuffer buffer = response.bufferFactory().wrap(errorBody.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }


    @Data
    @NoArgsConstructor
    public static class Config {
        // Example: can be used to pass route-specific configurations to the filter
        private List<String> requiredScopes;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("requiredScopes");
    }
}