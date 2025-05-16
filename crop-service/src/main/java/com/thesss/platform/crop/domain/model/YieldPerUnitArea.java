package com.thesss.platform.crop.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

// This is a Value Object, not an Entity. It is not persisted directly.
public class YieldPerUnitArea {

    private final BigDecimal value;
    private final String unit; // Derived unit, e.g., 'kg/ha'

    public YieldPerUnitArea(BigDecimal value, String unit) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Yield per unit area value cannot be null or negative");
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Yield unit cannot be null or empty");
        }
        this.value = value;
        this.unit = unit;
    }

    public BigDecimal getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YieldPerUnitArea that = (YieldPerUnitArea) o;
        // For BigDecimal, use compareTo for equality to ignore scale differences if needed.
        // For simplicity here, Objects.equals which uses BigDecimal.equals (scale matters).
        return Objects.equals(value, that.value) &&
               Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }

    @Override
    public String toString() {
        // Provides a display-friendly representation
        return value.stripTrailingZeros().toPlainString() + " " + unit;
    }
}