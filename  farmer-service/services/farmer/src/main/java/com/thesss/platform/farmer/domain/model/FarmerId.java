package com.thesss.platform.farmer.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing the unique identifier for a Farmer.
 * REQ-FRM-002
 */
public final class FarmerId implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID value;

    private FarmerId(UUID value) {
        this.value = Objects.requireNonNull(value, "FarmerId UUID value cannot be null");
    }

    public static FarmerId generate() {
        return new FarmerId(UUID.randomUUID());
    }

    public static FarmerId of(UUID value) {
        return new FarmerId(value);
    }

    public static FarmerId fromString(String uuidString) {
        Objects.requireNonNull(uuidString, "UUID string cannot be null");
        return new FarmerId(UUID.fromString(uuidString));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FarmerId farmerId = (FarmerId) o;
        return Objects.equals(value, farmerId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}