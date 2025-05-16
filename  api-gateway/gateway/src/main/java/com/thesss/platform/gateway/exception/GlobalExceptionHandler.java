package com.thesss.platform.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// Assuming ApiErrorResponse DTO exists in this package or is accessible
// import com.thesss.platform.gateway.dto.ApiErrorResponse; // If in DTO package
// For this exercise, we'll assume ApiErrorResponse record/class is defined like this:
// package com.thesss.platform.gateway.exception;
// public record ApiErrorResponse(long timestamp, int status, String error, String message, String path, String correlationId, String errorCode, List<String> details) {}
// Note: The field `correlationId` was added, it was not in the initial spec for ApiErrorResponse, but useful for error handling.
// The original spec for ApiErrorResponse was: timestamp (long), status (int), error (String), message (String), path (String), errorCode (String, optional), details (List<String>, optional)
// I will stick to the original definition and try to get correlationId from the exchange if available.

@Component
@Order(-1) // Ensures this handler has high precedence
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ObjectMapper objectMapper;

    // Placeholder for ApiErrorResponse DTO
    // This should be a proper class/record in its own file: com/thesss/platform/gateway/exception/ApiErrorResponse.java
    // As per SDS: Fields: ["timestamp", "status", "error", "message", "path", "errorCode", "details"]
    public record ApiErrorResponse(
        long timestamp,
        int status,
        String error,
        String message,
        String path,
        String errorCode, // optional
        List<String> details // optional
        // String correlationId // Consider adding this from exchange attributes
    ) {}


    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        String path = exchange.getRequest().getPath().value();
        String correlationId = exchange.getAttribute("correlationId"); // Assuming set by a logging filter

        log.error("Error occurred for request path: {} [CorrelationID: {}]. Exception: ", path, correlationId, ex);

        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "An unexpected error occurred.";
        String errorCode = "GW-UNKNOWN-ERROR";
        List<String> details = Collections.emptyList();

        if (ex instanceof ResponseStatusException rse) {
            httpStatus = HttpStatus.valueOf(rse.getStatusCode().value());
            message = rse.getReason() != null ? rse.getReason() : httpStatus.getReasonPhrase();
            errorCode = "GW-" + httpStatus.value();
        } else if (ex instanceof AuthenticationException) {
            httpStatus = HttpStatus.UNAUTHORIZED;
            message = "Authentication failed: " + ex.getMessage();
            errorCode = "GW-AUTH-001";
        } else if (ex instanceof AccessDeniedException) {
            httpStatus = HttpStatus.FORBIDDEN;
            message = "Access denied: " + ex.getMessage();
            errorCode = "GW-AUTH-002";
        } else if (ex instanceof org.springframework.cloud.gateway.support.NotFoundException nfe) {
            httpStatus = HttpStatus.NOT_FOUND;
            message = "Resource not found: " + nfe.getReason(); // Or a more generic "API endpoint not found"
            errorCode = "GW-ROUTE-001";
        } else if (ex instanceof IllegalArgumentException iae) {
            httpStatus = HttpStatus.BAD_REQUEST;
            message = "Invalid request: " + iae.getMessage();
            errorCode = "GW-VALIDATION-001";
            // Potentially parse details from BindException if applicable
        }
        // Add more specific exception handling as needed (e.g., custom exceptions from filters)
        // else if (ex instanceof ApiKeyValidationException) { ... }
        // else if (ex instanceof ConsentNotGrantedException) { ... }


        ApiErrorResponse errorResponse = new ApiErrorResponse(
                System.currentTimeMillis(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                message,
                path,
                errorCode,
                details
        );

        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] responseBytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = bufferFactory.wrap(responseBytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error writing JSON error response: {}", e.getMessage(), e);
            // Fallback to a simpler response if JSON processing fails
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            DataBuffer buffer = bufferFactory.wrap("{\"error\":\"Internal Server Error\"}".getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }
}