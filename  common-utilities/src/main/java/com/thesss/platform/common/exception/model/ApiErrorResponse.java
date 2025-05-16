package com.thesss.platform.common.exception.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thesss.platform.common.logging.util.MDCContext;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String correlationId;
    private List<ApiValidationError> validationErrors; // Optional

    public ApiErrorResponse() {
        this.timestamp = Instant.now();
        this.correlationId = MDCContext.getCorrelationId() != null ? MDCContext.getCorrelationId() : UUID.randomUUID().toString();
    }

    public ApiErrorResponse(int status, String error, String message, String path, String correlationId, List<ApiValidationError> validationErrors) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.correlationId = correlationId != null ? correlationId : MDCContext.getCorrelationId();
        if (this.correlationId == null) { // Ultimate fallback
            this.correlationId = UUID.randomUUID().toString();
        }
        this.validationErrors = validationErrors;
    }

    public ApiErrorResponse(int status, String error, String message, String path, String correlationId) {
        this(status, error, message, path, correlationId, null);
    }

    // Getters and Setters

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public List<ApiValidationError> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<ApiValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }

    @Override
    public String toString() {
        return "ApiErrorResponse{" +
               "timestamp=" + timestamp +
               ", status=" + status +
               ", error='" + error + '\'' +
               ", message='" + message + '\'' +
               ", path='" + path + '\'' +
               ", correlationId='" + correlationId + '\'' +
               ", validationErrors=" + validationErrors +
               '}';
    }
}