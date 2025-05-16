package com.thesss.platform.land.domain.model;

import lombok.Value;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representing an area, including its numerical value and unit of measure.
 * REQ-2-006
 */
@Value // Lombok annotation for immutable class
public class Area {
    BigDecimal value;
    String unit; // e.g., "HA" (Hectares), "AC" (Acres), "SQM" (Square Meters) - Code from Master Data

    public Area(BigDecimal value, String unit) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Area value must be non-null and non-negative.");
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Area unit cannot be null or blank.");
        }
        this.value = value;
        this.unit = unit.toUpperCase(); // Standardize unit case
    }

    // Potential methods for comparison or conversion could be added here if they
    // don't rely on external services like a UnitConversionService.
    // For example, simple checks or operations if units are assumed to be consistent.
}