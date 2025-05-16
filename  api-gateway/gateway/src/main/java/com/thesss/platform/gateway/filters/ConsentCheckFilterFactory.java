package com.thesss.platform.gateway.filters;

import com.thesss.platform.gateway.service.ConsentServiceClient;
import com.thesss.platform.gateway.dto.ConsentStatusResponse; // Assuming this DTO exists

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;


@Component
public class ConsentCheckFilterFactory extends AbstractGatewayFilterFactory<ConsentCheckFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(ConsentCheckFilterFactory.class);

    private final ConsentServiceClient consentServiceClient;

    @Autowired
    public ConsentCheckFilterFactory(ConsentServiceClient consentServiceClient) {
        super(Config.class);
        this.consentServiceClient = consentServiceClient;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (!StringUtils.hasText(config.getDataScope())) {
                log.error("DataScope is not configured for ConsentCheckFilter. Denying request.");
                return forbidden(exchange, "Consent check configuration error: DataScope missing.");
            }

            // Extract farmerId from path variables
            // Assumes a route pattern like /api/v1/farmer-data/{farmerId}/**
            Map<String, String> uriVariables = ServerWebExchangeUtils.getUriTemplateVariables(exchange);
            String farmerId = uriVariables.get("farmerId"); // Ensure your route definition captures 'farmerId'

            if (!StringUtils.hasText(farmerId)) {
                log.warn("FarmerId not found in path variables for consent check. URI: {}", exchange.getRequest().getURI());
                return forbidden(exchange, "Farmer identifier missing for consent check.");
            }

            // Extract clientId from attributes set by a previous authentication filter
            String clientId = exchange.getAttribute(AuthenticationFilterFactory.AUTHENTICATED_CLIENT_ID_ATTR);
            if (!StringUtils.hasText(clientId)) {
                // Fallback: try to get from JWT principal if available and applicable
                // For now, strict dependency on API Key auth context
                log.warn("ClientId not found in exchange attributes for consent check. URI: {}", exchange.getRequest().getURI());
                return forbidden(exchange, "Client identifier missing for consent check.");
            }

            log.debug("Performing consent check for FarmerId: {}, ClientId: {}, DataScope: {}", farmerId, clientId, config.getDataScope());

            return consentServiceClient.verifyConsent(farmerId, config.getDataScope(), clientId)
                .flatMap(consentResponse -> {
                    if (consentResponse.isConsentGranted()) {
                        log.info("Consent granted for FarmerId: {}, ClientId: {}, DataScope: {}", farmerId, clientId, config.getDataScope());
                        return chain.filter(exchange);
                    } else {
                        log.warn("Consent denied for FarmerId: {}, ClientId: {}, DataScope: {}", farmerId, clientId, config.getDataScope());
                        return forbidden(exchange, "Consent not granted for the requested data access.");
                    }
                })
                .onErrorResume(ex -> {
                    log.error("Error during consent verification for FarmerId: {}, ClientId: {}, DataScope: {}",
                            farmerId, clientId, config.getDataScope(), ex);
                    return forbidden(exchange, "Consent verification failed.");
                });
        };
    }

    private Mono<Void> forbidden(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        // Optionally, write a standard error response body
        // For now, just setting status and completing.
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        private String dataScope;

        public String getDataScope() {
            return dataScope;
        }

        public void setDataScope(String dataScope) {
            this.dataScope = dataScope;
        }
    }
}