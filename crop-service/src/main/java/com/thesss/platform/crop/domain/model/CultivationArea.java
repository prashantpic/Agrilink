package com.thesss.platform.crop.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class CultivationArea {

    @Column(name = "cultivated_area_value", precision = 12, scale = 3, nullable = false)
    private BigDecimal areaValue;

    @Column(name = "cultivated_area_unit_master_id", nullable = false)
    private String areaUnitMasterId; // References MasterData ID for unit

    protected CultivationArea() {
        // JPA LOMBOK Requirement
    }

    public CultivationArea(BigDecimal areaValue, String areaUnitMasterId) {
        if (areaValue == null || areaValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Area value must be positive");
        }
        if (areaUnitMasterId == null || areaUnitMasterId.isBlank()) {
            throw new IllegalArgumentException("Area unit master ID cannot be null or empty");
        }
        this.areaValue = areaValue;
        this.areaUnitMasterId = areaUnitMasterId;
    }

    public BigDecimal getAreaValue() {
        return areaValue;
    }

    public String getAreaUnitMasterId() {
        return areaUnitMasterId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CultivationArea that = (CultivationArea) o;
        return Objects.equals(areaValue, that.areaValue) &&
               Objects.equals(areaUnitMasterId, that.areaUnitMasterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(areaValue, areaUnitMasterId);
    }
}