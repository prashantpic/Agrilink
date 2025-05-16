package com.thesss.platform.common.dtos;

import java.time.Instant;
import com.thesss.platform.common.logging.util.MDCContext;

public class GenericApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Instant timestamp;
    private String correlationId;

    public GenericApiResponse() {
        this.timestamp = Instant.now();
        this.correlationId = MDCContext.getCorrelationId();
    }

    public GenericApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
        this.correlationId = MDCContext.getCorrelationId();
    }

    public static <T> GenericApiResponse<T> success(T data) {
        return new GenericApiResponse<>(true, "Operation successful", data);
    }

    public static <T> GenericApiResponse<T> success(String message, T data) {
        return new GenericApiResponse<>(true, message, data);
    }

    public static <T> GenericApiResponse<T> error(String message) {
         GenericApiResponse<T> response = new GenericApiResponse<>();
         response.setSuccess(false);
         response.setMessage(message);
         return response;
    }

    // Getters and Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public String toString() {
        return "GenericApiResponse{" +
               "success=" + success +
               ", message='" + message + '\'' +
               ", data=" + data +
               ", timestamp=" + timestamp +
               ", correlationId='" + correlationId + '\'' +
               '}';
    }
}