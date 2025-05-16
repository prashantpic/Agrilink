package com.thesss.platform.land.domain.model;

import lombok.Value;
import java.util.UUID;

/**
 * Value Object representing the unique identifier (UUID) of the farmer.
 * REQ-2-003
 */
@Value // Lombok annotation for immutable class
public class FarmerId {
    UUID value;

    private FarmerId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("FarmerId value cannot be null.");
        }
        this.value = value;
    }

    public static FarmerId of(UUID value) {
        return new FarmerId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}