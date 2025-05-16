package com.thesss.platform.land.domain.exception;

/**
 * Custom domain exception for invalid JTS geometry (e.g., self-intersecting polygon, not enough points).
 * REQ-2-020, REQ-1.3-003
 */
public class InvalidGeometryException extends RuntimeException {

    public InvalidGeometryException(String message) {
        super(message);
    }

    public InvalidGeometryException(String message, Throwable cause) {
        super(message, cause);
    }
}