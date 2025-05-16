package com.thesss.platform.land.domain.model;

import lombok.Value;
import java.util.UUID;

/**
 * Value Object representing the unique identifier (UUID) for a FarmLandRecord.
 * REQ-2-002
 */
@Value // Lombok annotation for immutable class with equals, hashCode, toString, and constructor
public class LandRecordId {
    UUID value;

    private LandRecordId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("LandRecordId value cannot be null.");
        }
        this.value = value;
    }

    public static LandRecordId of(UUID value) {
        return new LandRecordId(value);
    }

    public static LandRecordId random() {
        return new LandRecordId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}