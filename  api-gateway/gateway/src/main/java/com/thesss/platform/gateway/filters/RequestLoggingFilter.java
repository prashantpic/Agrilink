package com.thesss.platform.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String CORRELATION_ID_HEADER_NAME = "X-Request-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String REQUEST_START_TIME_ATTR = "requestStartTime";
    public static final String CORRELATION_ID_ATTR = "correlationIdAttr";


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(REQUEST_START_TIME_ATTR, startTime);

        ServerHttpRequest request = exchange.getRequest();
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER_NAME);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        exchange.getAttributes().put(CORRELATION_ID_ATTR, correlationId);

        // Add correlationId to response headers
        String finalCorrelationId = correlationId;
        exchange.getResponse().beforeCommit(() -> {
            exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER_NAME, finalCorrelationId);
            return Mono.empty();
        });

        log.info("Incoming request: Method={}, URI={}, Headers={}, RemoteAddress={}, CorrelationID={}",
                request.getMethod(),
                request.getURI(),
                request.getHeaders(),
                request.getRemoteAddress(),
                correlationId);

        return chain.filter(exchange)
                .doFinally(signalType -> MDC.remove(CORRELATION_ID_MDC_KEY));
    }

    @Override
    public int getOrder() {
        // Run this filter early in the chain
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}