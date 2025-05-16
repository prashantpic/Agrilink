package com.thesss.platform.common.exception.handler;

import com.thesss.platform.common.exception.model.ApiErrorResponse;
import com.thesss.platform.common.exception.model.ApiValidationError;
import com.thesss.platform.common.exception.types.BaseApplicationException;
import com.thesss.platform.common.exception.types.ValidationException;
import com.thesss.platform.common.logging.util.MDCContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

    @ExceptionHandler(BaseApplicationException.class)
    public ResponseEntity<ApiErrorResponse> handleBaseApplicationException(BaseApplicationException ex, WebRequest request) {
        HttpStatus status = ex.getHttpStatus() != null ? ex.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
        String path = getRequestPath(request);
        String correlationId = getCorrelationId();

        List<ApiValidationError> validationErrors = null;
        if (ex instanceof ValidationException) {
            validationErrors = ((ValidationException) ex).getErrors();
        }

        ApiErrorResponse apiError = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                path,
                correlationId,
                validationErrors
        );

        log.error("BaseApplicationException caught: Status={}, Path={}, Message='{}', CorrelationId={}",
                  status.value(), path, ex.getMessage(), correlationId, ex);
        return new ResponseEntity<>(apiError, status);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String path = getRequestPath(request);
        String correlationId = getCorrelationId();

        List<ApiValidationError> validationErrors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            validationErrors.add(new ApiValidationError(error.getField(), error.getRejectedValue(), error.getDefaultMessage()));
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            validationErrors.add(new ApiValidationError(error.getObjectName(), null, error.getDefaultMessage()));
        }

        ApiErrorResponse apiError = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",
                path,
                correlationId,
                validationErrors
        );
        log.warn("MethodArgumentNotValidException caught: Path={}, Errors={}, CorrelationId={}",
                 path, validationErrors.size(), correlationId);
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        String path = getRequestPath(request);
        String correlationId = getCorrelationId();

        ApiErrorResponse apiError = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "Access Denied: You do not have permission to access this resource.",
                path,
                correlationId
        );
        log.warn("AccessDeniedException caught: Path={}, Message='{}', CorrelationId={}",
                 path, ex.getMessage(), correlationId);
        return new ResponseEntity<>(apiError, status);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String path = getRequestPath(request);
        String correlationId = getCorrelationId();

        ApiErrorResponse apiError = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                "An unexpected internal server error occurred. Please contact support.",
                path,
                correlationId
        );
        log.error("Generic Uncaught Exception caught: Path={}, Message='{}', CorrelationId={}",
                  path, ex.getMessage(), correlationId, ex);
        return new ResponseEntity<>(apiError, status);
    }

    private String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        return "N/A";
    }

    private String getCorrelationId() {
        String correlationId = MDCContext.getCorrelationId();
        if (correlationId == null) {
            // Fallback if MDC is not populated, though it should be by CorrelationIdFilter
            correlationId = UUID.randomUUID().toString();
            log.warn("Correlation ID not found in MDC. Generated a fallback: {}", correlationId);
        }
        return correlationId;
    }
}