package com.thesss.platform.land.domain.exception;

/**
 * Custom domain exception for parcel ID uniqueness conflicts within a defined administrative region.
 * REQ-2-004
 */
public class ParcelIdConflictException extends RuntimeException {

    public ParcelIdConflictException(String parcelId, String region) {
        super("Parcel ID '" + parcelId + "' already exists in region '" + region + "'.");
    }

    public ParcelIdConflictException(String message) {
        super(message);
    }

    public ParcelIdConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}