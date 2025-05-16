package com.thesss.platform.common.logging.filter;

import com.thesss.platform.common.logging.util.MDCContext;
import com.thesss.platform.common.security.config.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String correlationId = request.getHeader(SecurityConstants.CORRELATION_ID_HEADER_NAME);

        if (correlationId == null || correlationId.isEmpty() || "null".equalsIgnoreCase(correlationId)) {
            correlationId = UUID.randomUUID().toString();
            log.debug("No Correlation ID found in request header '{}'. Generating new one: {}", SecurityConstants.CORRELATION_ID_HEADER_NAME, correlationId);
        } else {
            log.debug("Using Correlation ID from request header '{}': {}", SecurityConstants.CORRELATION_ID_HEADER_NAME, correlationId);
        }

        MDCContext.setCorrelationId(correlationId);
        // Also set it as a request attribute to be accessible by GlobalApiExceptionHandler if MDC is cleared early
        request.setAttribute(MDCContext.CORRELATION_ID_KEY, correlationId);


        // Add/overwrite the correlation ID in the HTTP response header
        if (!response.containsHeader(SecurityConstants.CORRELATION_ID_HEADER_NAME)) {
            response.setHeader(SecurityConstants.CORRELATION_ID_HEADER_NAME, correlationId);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clear MDC after the request is finished to prevent context leakage in thread pools
            MDCContext.clear(); // Clears all MDC entries for this thread
            log.debug("Correlation ID {} and other MDC context cleared.", correlationId);
        }
    }
}