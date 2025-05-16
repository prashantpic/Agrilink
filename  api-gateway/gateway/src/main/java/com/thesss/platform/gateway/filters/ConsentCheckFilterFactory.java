package com.thesss.platform.gateway.filters;

import com.thesss.platform.gateway.service.ConsentServiceClient;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ConsentCheckFilterFactory extends AbstractGatewayFilterFactory<ConsentCheckFilterFactory.Config> {

    private final ConsentServiceClient consentServiceClient;

    @Value("${gateway.feature-toggles.enable-consent-filter-for-farmer-data-api:true}")
    private boolean consentFilterEnabled;

    @Autowired
    public ConsentCheckFilterFactory(ConsentServiceClient consentServiceClient) {
        super(Config.class);
        this.consentServiceClient = consentServiceClient;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!consentFilterEnabled) {
                log.debug("Consent check filter is disabled globally. Skipping consent check for dataScope: {}", config.getDataScope());
                return chain.filter(exchange);
            }

            if (!StringUtils.hasText(config.getDataScope())) {
                log.error("Data scope is not configured for ConsentCheckFilter. Denying access.");
                return errorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Consent check configuration error: Data scope missing.");
            }

            // Extract Farmer ID from path variables
            Map<String, String> uriVariables = exchange.getAttribute(ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            String farmerId = (uriVariables != null) ? uriVariables.get("farmerId") : null;

            if (!StringUtils.hasText(farmerId)) {
                // Fallback: Try to get farmerId from a pre-authenticated principal or custom header if available
                // For this example, we rely on path variable. If not present, it's a configuration error or bad request.
                log.warn("Farmer ID not found in path variables for consent check. Path: {}", exchange.getRequest().getPath());
                return errorResponse(exchange, HttpStatus.BAD_REQUEST, "Farmer ID missing for consent check.");
            }

            // Extract Client ID (assuming it was set by AuthenticationFilterFactory)
            String clientId = exchange.getAttribute(AuthenticationFilterFactory.AUTHENTICATED_CLIENT_ID_ATTR);
            if (!StringUtils.hasText(clientId)) {
                log.warn("Client ID not found in exchange attributes for consent check. API Key authentication might be missing or did not set the attribute.");
                return errorResponse(exchange, HttpStatus.UNAUTHORIZED, "Client identification missing for consent check.");
            }

            log.debug("Performing consent check for Farmer ID: {}, Client ID: {}, Data Scope: {}", farmerId, clientId, config.getDataScope());

            return consentServiceClient.verifyConsent(farmerId, config.getDataScope(), clientId)
                .flatMap(consentStatusResponse -> {
                    if (consentStatusResponse.isConsentGranted()) {
                        log.info("Consent granted for Farmer ID: {}, Client ID: {}, Data Scope: {}", farmerId, clientId, config.getDataScope());
                        return chain.filter(exchange);
                    } else {
                        log.warn("Consent denied for Farmer ID: {}, Client ID: {}, Data Scope: {}", farmerId, clientId, config.getDataScope());
                        return errorResponse(exchange, HttpStatus.FORBIDDEN, "Access denied. Required consent not granted for the requested data scope.");
                    }
                })
                .onErrorResume(throwable -> {
                    log.error("Error during consent verification call for Farmer ID: {}, Client ID: {}, Data Scope: {}", farmerId, clientId, config.getDataScope(), throwable);
                    return errorResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Consent verification service error.");
                });
        };
    }

    private Mono<Void> errorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        // Using a simplified error structure here, actual response should use ApiErrorResponse via GlobalExceptionHandler
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
        private String dataScope;
    }
    
    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("dataScope");
    }
}