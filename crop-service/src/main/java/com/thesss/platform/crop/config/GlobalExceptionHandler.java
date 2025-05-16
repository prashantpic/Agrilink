package com.thesss.platform.crop.config;

import com.thesss.platform.crop.domain.exceptions.CalculationException;
import com.thesss.platform.crop.domain.exceptions.CropCycleNotFoundException;
import com.thesss.platform.crop.domain.exceptions.ExternalServiceException;
import com.thesss.platform.crop.domain.exceptions.InvalidCropCycleStateTransitionException;
import com.thesss.platform.crop.domain.exceptions.MasterDataResolutionException;
import com.thesss.platform.crop.domain.exceptions.ValidationException; // Generic domain validation
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<Object> buildErrorResponse(Exception ex, HttpStatus status, WebRequest request, String errorCode) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        if (errorCode != null) {
            body.put("errorCode", errorCode);
        }
        logger.error("Exception: {} - Code: {} - Path: {}", ex.getMessage(), errorCode, body.get("path"), ex);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(CropCycleNotFoundException.class)
    public ResponseEntity<Object> handleCropCycleNotFoundException(CropCycleNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request, "CROP_CYCLE_NOT_FOUND");
    }

    @ExceptionHandler(InvalidCropCycleStateTransitionException.class)
    public ResponseEntity<Object> handleInvalidCropCycleStateTransitionException(InvalidCropCycleStateTransitionException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request, "INVALID_STATE_TRANSITION");
    }

    @ExceptionHandler(ValidationException.class) // Custom domain validation exception
    public ResponseEntity<Object> handleDomainValidationException(ValidationException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request, "DOMAIN_VALIDATION_ERROR");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class) // Jakarta Bean Validation
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        body.put("message", "Validation failed: " + errors);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        body.put("errorCode", "REQUEST_VALIDATION_ERROR");
        logger.warn("Request validation error: {} - Path: {}", body.get("message"), body.get("path"));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Object> handleFeignException(FeignException ex, WebRequest request) {
        logger.error("Feign client error: Status {} - Method {} - Message: {}", ex.status(), ex.request() != null ? ex.request().httpMethod() : "N/A", ex.getMessage());
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR; // Default if status code is not standard
        }
        // Attempt to parse error content from FeignException if available
        String feignErrorMessage = ex.contentUTF8();
        String message = "Error communicating with external service: " + (feignErrorMessage.isEmpty() ? ex.getMessage() : feignErrorMessage);

        return buildErrorResponse(new ExternalServiceException(message, ex), status, request, "EXTERNAL_SERVICE_ERROR");
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<Object> handleExternalServiceException(ExternalServiceException ex, WebRequest request) {
        // This can be used if ports/adapters throw this specific exception
        return buildErrorResponse(ex, HttpStatus.SERVICE_UNAVAILABLE, request, "EXTERNAL_SERVICE_COMMUNICATION_FAILURE");
    }


    @ExceptionHandler(MasterDataResolutionException.class)
    public ResponseEntity<Object> handleMasterDataResolutionException(MasterDataResolutionException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request, "MASTER_DATA_RESOLUTION_ERROR");
    }

    @ExceptionHandler(CalculationException.class)
    public ResponseEntity<Object> handleCalculationException(CalculationException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request, "CALCULATION_ERROR");
    }


    @ExceptionHandler(IllegalArgumentException.class) // Catch common illegal arguments
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request, "ILLEGAL_ARGUMENT");
    }


    @ExceptionHandler(Exception.class) // Fallback for any other unhandled exceptions
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request, "INTERNAL_SERVER_ERROR");
    }
}