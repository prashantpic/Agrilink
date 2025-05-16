package com.thesss.platform.land.domain.exception;

/**
 * Custom domain exception for invalid area values (e.g., cultivable area > total area).
 * REQ-2-006
 */
public class InvalidAreaException extends RuntimeException {

    public InvalidAreaException(String message) {
        super(message);
    }

    public InvalidAreaException(String message, Throwable cause) {
        super(message, cause);
    }
}