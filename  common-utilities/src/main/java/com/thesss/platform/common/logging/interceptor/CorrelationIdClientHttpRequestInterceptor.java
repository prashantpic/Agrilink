package com.thesss.platform.common.logging.interceptor;

import com.thesss.platform.common.logging.util.MDCContext;
import com.thesss.platform.common.security.config.SecurityConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class CorrelationIdClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdClientHttpRequestInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String correlationId = MDCContext.getCorrelationId();

        if (correlationId != null && !correlationId.isEmpty()) {
            // Ensure the header is not already present, or decide if it should be overwritten.
            // Here, we add it if not present, or overwrite if already there.
            request.getHeaders().set(SecurityConstants.CORRELATION_ID_HEADER_NAME, correlationId);
            log.debug("Propagating Correlation ID {} to outgoing RestTemplate request header '{}' for URI: {}",
                      correlationId, SecurityConstants.CORRELATION_ID_HEADER_NAME, request.getURI());
        } else {
             log.debug("No Correlation ID found in MDC to propagate for RestTemplate request to URI: {}", request.getURI());
        }

        return execution.execute(request, body);
    }
}