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
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange)
            .doFinally(signalType -> {
                ServerHttpResponse response = exchange.getResponse();
                Long startTime = exchange.getAttribute(RequestLoggingFilter.REQUEST_START_TIME_ATTR);
                String correlationId = exchange.getAttribute(RequestLoggingFilter.CORRELATION_ID_ATTR);

                if (correlationId != null) {
                    MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
                }

                long duration = -1;
                if (startTime != null) {
                    duration = System.currentTimeMillis() - startTime;
                }

                Integer statusCode = response.getRawStatusCode();
                Long contentLength = response.getHeaders().getContentLength();


                log.info("Outgoing response: Status={}, Duration={}ms, ContentLength={}, CorrelationID={}, Headers={}",
                        statusCode,
                        duration,
                        contentLength == -1 ? "N/A" : contentLength,
                        correlationId,
                        response.getHeaders());

                if (correlationId != null) {
                    MDC.remove(CORRELATION_ID_MDC_KEY);
                }
            });
    }

    @Override
    public int getOrder() {
        // Run this filter late in the chain, but before the actual send.
        // NettyWriteResponseFilter is -1. We want to log after it has processed the response status and headers.
        return Ordered.LOWEST_PRECEDENCE - 2;
    }
}