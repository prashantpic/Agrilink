package com.thesss.platform.farmer.exception;

public class DuplicateFarmerResourceException extends RuntimeException {

    public DuplicateFarmerResourceException(String resourceType, String value) {
        super("Duplicate " + resourceType + " found: " + value);
    }

    public DuplicateFarmerResourceException(String message) {
        super(message);
    }

    public DuplicateFarmerResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}