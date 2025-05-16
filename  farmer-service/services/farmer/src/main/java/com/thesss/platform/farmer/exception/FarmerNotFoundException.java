package com.thesss.platform.farmer.exception;

import java.util.UUID;

public class FarmerNotFoundException extends RuntimeException {

    public FarmerNotFoundException(UUID farmerId) {
        super("Farmer not found with ID: " + farmerId);
    }

    public FarmerNotFoundException(String message) {
        super(message);
    }

    public FarmerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}