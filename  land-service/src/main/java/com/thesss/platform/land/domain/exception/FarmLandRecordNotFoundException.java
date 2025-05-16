package com.thesss.platform.land.domain.exception;

import com.thesss.platform.land.domain.model.LandRecordId;

/**
 * Custom domain exception for cases where a farm land record is not found by its ID.
 * REQ-2-001
 */
public class FarmLandRecordNotFoundException extends RuntimeException {

    public FarmLandRecordNotFoundException(LandRecordId landRecordId) {
        super("FarmLandRecord with ID '" + landRecordId.getValue() + "' not found.");
    }

    public FarmLandRecordNotFoundException(String message) {
        super(message);
    }

    public FarmLandRecordNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}