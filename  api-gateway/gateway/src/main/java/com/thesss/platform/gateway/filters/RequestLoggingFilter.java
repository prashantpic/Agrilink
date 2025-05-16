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
    public static final String CORRELATION_ID_HEADER_NAME = "X-Request-ID";
    public static final String TRACEPARENT_HEADER_NAME = "traceparent"; // W3C Trace Context
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";
    public static final String REQUEST_START_TIME_ATTR = "requestStartTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(REQUEST_START_TIME_ATTR, startTime);

        ServerHttpRequest request = exchange.getRequest();
        String correlationId = getOrGenerateCorrelationId(request);

        // Add correlation ID to response headers
        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER_NAME, correlationId);

        // Using MDC.putCloseableContext for try-with-resources style MDC management in reactive chains
        return Mono.deferContextual(contextView -> {
            try (MDC.MDCCloseable mdcCloseable = MDC.putCloseable(CORRELATION_ID_MDC_KEY, correlationId)) {
                log.info("Incoming Request: Method={}, URI={}, Path={}, Query={}, RemoteAddress={}, Headers={}, CorrelationID={}",
                        request.getMethod(),
                        request.getURI().toString(),
                        request.getPath(),
                        request.getQueryParams(),
                        request.getRemoteAddress(),
                        request.getHeaders(), // Be cautious logging all headers in production
                        correlationId);
            }
            return chain.filter(exchange);
        });
    }

    private String getOrGenerateCorrelationId(ServerHttpRequest request) {
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER_NAME);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = request.getHeaders().getFirst(TRACEPARENT_HEADER_NAME);
            // If traceparent exists, we could parse traceId from it, or use the whole string.
            // For simplicity, if traceparent exists but X-Request-ID doesn't, we use traceparent or generate new.
            // Here, let's prioritize X-Request-ID, then generate new if neither found.
            // A more sophisticated approach would parse traceparent for trace ID.
            if (correlationId == null || correlationId.isEmpty()) {
                 correlationId = UUID.randomUUID().toString();
                 log.debug("Generated new correlation ID: {}", correlationId);
            } else {
                 log.debug("Using traceparent as correlation ID: {}", correlationId);
            }
        } else {
            log.debug("Using existing correlation ID from header {}: {}", CORRELATION_ID_HEADER_NAME, correlationId);
        }
        return correlationId;
    }

    @Override
    public int getOrder() {
        // Run early, but allow for very early infrastructure filters
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}