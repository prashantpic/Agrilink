package com.thesss.platform.gateway.filters;

import com.thesss.platform.gateway.service.AuthServiceClient;
import com.thesss.platform.gateway.dto.ApiKeyValidationResponse; // Assuming this DTO exists

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;

@Component
public class AuthenticationFilterFactory extends AbstractGatewayFilterFactory<AuthenticationFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilterFactory.class);
    private static final String API_KEY_HEADER_NAME_PROPERTY = "${api.security.api-key.header-name:X-API-KEY}";
    public static final String AUTHENTICATED_CLIENT_ID_ATTR = "authenticatedClientId";
    public static final String AUTHENTICATED_CLIENT_SCOPES_ATTR = "authenticatedClientScopes";

    private final AuthServiceClient authServiceClient;

    @Value("${api.security.api-key.use-remote-service:true}")
    private boolean useRemoteAuthServiceForApiKeys;

    @Value(API_KEY_HEADER_NAME_PROPERTY)
    private String apiKeyHeaderName;

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
                return unauthorized(exchange, "Missing API Key");
            }

            String apiKey = Objects.requireNonNull(request.getHeaders().getFirst(apiKeyHeaderName));

            if (useRemoteAuthServiceForApiKeys) {
                return authServiceClient.validateApiKey(apiKey)
                    .flatMap(response -> {
                        if (response.isValid()) {
                            log.debug("API Key validated successfully for client: {}", response.getClientId());
                            exchange.getAttributes().put(AUTHENTICATED_CLIENT_ID_ATTR, response.getClientId());
                            exchange.getAttributes().put(AUTHENTICATED_CLIENT_SCOPES_ATTR, response.getScopes());
                            return chain.filter(exchange);
                        } else {
                            log.warn("Invalid API Key provided. Reason: {}", response.getErrorMessage());
                            return unauthorized(exchange, "Invalid API Key: " + response.getErrorMessage());
                        }
                    })
                    .onErrorResume(ex -> {
                        log.error("Error validating API Key via AuthService", ex);
                        return unauthorized(exchange, "API Key validation error");
                    });
            } else {
                // Placeholder for local/simple validation if useRemoteAuthServiceForApiKeys is false
                // For now, assume all keys are invalid in this simplified local mode or implement basic check
                log.warn("Local API Key validation is active (simple mode). Denying request with API Key: {}", apiKey.substring(0, Math.min(apiKey.length(), 8)) + "...");
                return unauthorized(exchange, "API Key validation not supported in current mode");
            }
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add(HttpHeaders.WWW_AUTHENTICATE, "ApiKey realm=\"Restricted Area\"");
        // Optionally, write a standard error response body if ApiErrorResponse is available
        // For now, just setting status and completing.
        // DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(("{\"error\":\"Unauthorized\", \"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8));
        // return exchange.getResponse().writeWith(Mono.just(buffer));
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Configuration properties for the filter, if any are needed per-route
        // e.g., private String requiredScope;
        // public String getRequiredScope() { return requiredScope; }
        // public void setRequiredScope(String requiredScope) { this.requiredScope = requiredScope; }
    }
}