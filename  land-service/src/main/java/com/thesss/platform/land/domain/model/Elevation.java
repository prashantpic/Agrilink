package com.thesss.platform.land.domain.model;

import lombok.Value;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representing land elevation, including its numerical value and unit of measure.
 * REQ-2-016
 */
@Value // Lombok annotation for immutable class
public class Elevation {
    BigDecimal value;
    String unit; // e.g., "M" (Meters), "FT" (Feet) - Code from Master Data

    public Elevation(BigDecimal value, String unit) {
        if (value == null) {
            throw new IllegalArgumentException("Elevation value cannot be null.");
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Elevation unit cannot be null or blank.");
        }
        this.value = value;
        this.unit = unit.toUpperCase(); // Standardize unit case
    }
}