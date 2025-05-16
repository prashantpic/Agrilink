package com.thesss.platform.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thesss.platform.gateway.dto.ApiErrorResponse; // Assuming DTO path
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
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

@Component
@Order(-1) // Ensure this handler has high precedence
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ObjectMapper objectMapper;

    // Standard header for Correlation ID, assuming RequestLoggingFilter populates this.
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_ATTRIBUTE = "correlationId";


    public GlobalExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorMessage = "An unexpected error occurred.";
        String errorCode = "GENERAL_ERROR"; // Default application-specific error code

        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null) {
            correlationId = (String) exchange.getAttributes().getOrDefault(CORRELATION_ID_ATTRIBUTE, UUID.randomUUID().toString());
        }

        if (ex instanceof ResponseStatusException rse) {
            httpStatus = HttpStatus.valueOf(rse.getStatusCode().value());
            errorMessage = rse.getReason() != null ? rse.getReason() : httpStatus.getReasonPhrase();
            // Try to derive a more specific error code if possible
            errorCode = "HTTP_" + httpStatus.value();
        } else if (ex instanceof org.springframework.cloud.gateway.support.NotFoundException) {
            httpStatus = HttpStatus.NOT_FOUND;
            errorMessage = "The requested resource was not found.";
            errorCode = "NOT_FOUND";
        } else if (ex instanceof AccessDeniedException) {
            httpStatus = HttpStatus.FORBIDDEN;
            errorMessage = "Access denied. You do not have permission to access this resource.";
            errorCode = "ACCESS_DENIED";
        } else if (ex instanceof AuthenticationException) { // Covers various Spring Security auth errors
            httpStatus = HttpStatus.UNAUTHORIZED;
            errorMessage = "Authentication failed. Invalid credentials or token.";
            errorCode = "UNAUTHENTICATED";
        } else if (ex instanceof InvalidRequestException ire) { // Custom exception example
            httpStatus = HttpStatus.BAD_REQUEST;
            errorMessage = ire.getMessage();
            errorCode = ire.getErrorCode() != null ? ire.getErrorCode() : "INVALID_REQUEST";
        } else if (ex instanceof ServiceUnavailableException sue) { // Custom exception example for downstream issues
             httpStatus = HttpStatus.SERVICE_UNAVAILABLE;
             errorMessage = sue.getMessage();
             errorCode = sue.getErrorCode() != null ? sue.getErrorCode() : "SERVICE_UNAVAILABLE";
        }
        // Add more specific exception handling as needed, e.g., for custom filter exceptions

        log.error("Error occurred: [CorrelationID: {}] {} - Status: {}, Message: {}, Details: {}",
                correlationId, ex.getClass().getSimpleName(), httpStatus.value(), errorMessage, ex.getMessage(), ex);


        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .timestamp(Instant.now().toEpochMilli())
                .status(httpStatus.value())
                .error(httpStatus.getReasonPhrase())
                .message(errorMessage)
                .path(exchange.getRequest().getPath().value())
                .correlationId(correlationId)
                .errorCode(errorCode)
                .details(ex.getCause() != null ? Collections.singletonList(ex.getCause().getMessage()) : Collections.emptyList())
                .build();

        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] responseBytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = bufferFactory.wrap(responseBytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("[CorrelationID: {}] Error serializing error response: {}", correlationId, e.getMessage(), e);
            // Fallback to a simpler response if JSON serialization fails
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            DataBuffer buffer = bufferFactory.wrap("Internal Server Error".getBytes(StandardCharsets.UTF_8));
            return exchange.getResponse().writeWith(Mono.just(buffer));
        }
    }
}