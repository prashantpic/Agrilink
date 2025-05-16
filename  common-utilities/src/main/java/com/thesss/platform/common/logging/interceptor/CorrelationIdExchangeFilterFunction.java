package com.thesss.platform.common.logging.interceptor;

import com.thesss.platform.common.logging.util.MDCContext;
import com.thesss.platform.common.security.config.SecurityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

public class CorrelationIdExchangeFilterFunction implements ExchangeFilterFunction {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdExchangeFilterFunction.class);

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return Mono.deferContextual(contextView -> {
            String correlationId = getCorrelationIdFromContextOrMdc(contextView);

            ClientRequest newRequest = request;
            if (correlationId != null && !correlationId.isEmpty()) {
                newRequest = ClientRequest.from(request)
                        .header(SecurityConstants.CORRELATION_ID_HEADER_NAME, correlationId)
                        .build();
                log.debug("Propagating Correlation ID {} to outgoing WebClient request header '{}' for URI: {}",
                          correlationId, SecurityConstants.CORRELATION_ID_HEADER_NAME, request.url());
            } else {
                 log.debug("No Correlation ID found in Reactor Context or MDC to propagate for WebClient request to URI: {}", request.url());
            }
            return next.exchange(newRequest);
        });
    }

    private String getCorrelationIdFromContextOrMdc(ContextView contextView) {
        // Try getting from Reactor Context first (preferred in reactive streams)
        // The key used in Reactor context should be consistent.
        String correlationId = contextView.getOrDefault(MDCContext.CORRELATION_ID_KEY, null);

        // If not found in Reactor context, fall back to SLF4J MDC.
        // This fallback is useful if the reactive flow is initiated from a non-reactive (e.g., Servlet) context
        // where MDC might have been populated.
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = MDCContext.getCorrelationId();
        }
        return correlationId;
    }
}