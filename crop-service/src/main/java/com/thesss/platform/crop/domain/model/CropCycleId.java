package com.thesss.platform.crop.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CropCycleId implements Serializable {

    @Column(name = "crop_cycle_id_value", unique = true, nullable = false) // Explicit column name to avoid conflict if 'value' is a reserved keyword
    private UUID value;

    protected CropCycleId() {
        // JPA LOMBOK Requirement
    }

    public CropCycleId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("CropCycleId value cannot be null");
        }
        this.value = value;
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CropCycleId that = (CropCycleId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "null";
    }
}