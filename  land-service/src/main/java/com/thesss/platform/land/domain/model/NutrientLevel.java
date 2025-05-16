package com.thesss.platform.land.domain.model;

import lombok.Value;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representing a nutrient level (e.g., N, P, K, pH, Organic Carbon)
 * with its value and unit.
 * REQ-2-012
 */
@Value // Lombok annotation for immutable class
public class NutrientLevel {
    BigDecimal value;
    String unit; // e.g., "PPM", "KG_HA", "%" - Code from Master Data

    public NutrientLevel(BigDecimal value, String unit) {
        // Value can be null if not measured, but if provided, unit is mandatory
        if (value != null && (unit == null || unit.isBlank())) {
            throw new IllegalArgumentException("Nutrient unit cannot be null or blank if value is provided.");
        }
        // Some nutrient values might have specific range constraints (e.g., pH 0-14)
        // This can be added here or validated at a higher level (application service).
        // For simplicity, basic non-negativity if applicable:
        // if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
        //    throw new IllegalArgumentException("Nutrient value cannot be negative.");
        // }
        this.value = value;
        this.unit = (unit != null) ? unit.toUpperCase() : null; // Standardize unit case
    }
}