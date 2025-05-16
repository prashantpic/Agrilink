package com.thesss.platform.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class ResponseLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(ResponseLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
            .doFinally(signalType -> {
                // Retrieve correlationId from MDC, which should have been set by RequestLoggingFilter
                // or from exchange attributes if preferred.
                String correlationId = MDC.get(RequestLoggingFilter.CORRELATION_ID_MDC_KEY);
                if (correlationId == null) { // Fallback if MDC was cleared or not set
                    correlationId = exchange.getResponse().getHeaders().getFirst(RequestLoggingFilter.CORRELATION_ID_HEADER_NAME);
                }

                try (MDC.MDCCloseable mdcCloseable = MDC.putCloseable(RequestLoggingFilter.CORRELATION_ID_MDC_KEY, correlationId != null ? correlationId : "unknown")) {
                    ServerHttpResponse response = exchange.getResponse();
                    Long startTime = exchange.getAttribute(RequestLoggingFilter.REQUEST_START_TIME_ATTR);
                    long duration = -1;
                    if (startTime != null) {
                        duration = System.currentTimeMillis() - startTime;
                    }

                    log.info("Outgoing Response: CorrelationID={}, Status={}, Duration={}ms, Headers={}, ContentLength={}",
                            correlationId,
                            response.getStatusCode(),
                            duration,
                            response.getHeaders(), // Be cautious logging all headers
                            response.getHeaders().getContentLength() // May be -1 if not set
                    );
                } catch (Exception e) {
                    log.warn("Error during response logging", e);
                }
            });
    }

    @Override
    public int getOrder() {
        // Run late, but allow for very late infrastructure filters like NettyWriteResponseFilter
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}