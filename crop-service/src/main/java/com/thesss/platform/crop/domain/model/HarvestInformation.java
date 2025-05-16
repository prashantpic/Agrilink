package com.thesss.platform.crop.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class HarvestInformation {

    @Column(name = "actual_harvest_date")
    private LocalDate actualHarvestDate; // Optional until harvest

    @Column(name = "total_yield_quantity", precision = 12, scale = 3)
    private BigDecimal totalYieldQuantity; // Optional until harvest

    @Column(name = "total_yield_unit_master_id")
    private String totalYieldUnitMasterId; // References MasterData ID for unit, Optional until harvest

    @Column(name = "quality_grade_master_id")
    private String qualityGradeMasterId; // Optional, references MasterData ID

    protected HarvestInformation() {
        // JPA LOMBOK Requirement
    }

    public HarvestInformation(LocalDate actualHarvestDate, BigDecimal totalYieldQuantity, String totalYieldUnitMasterId, String qualityGradeMasterId) {
        if (totalYieldQuantity != null && totalYieldQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Total yield quantity cannot be negative.");
        }
        if ((totalYieldQuantity != null || totalYieldUnitMasterId != null) && actualHarvestDate == null) {
            throw new IllegalArgumentException("Actual harvest date must be provided if yield quantity or unit is present.");
        }
        if (totalYieldQuantity != null && (totalYieldUnitMasterId == null || totalYieldUnitMasterId.isBlank())) {
            throw new IllegalArgumentException("Total yield unit must be provided if yield quantity is present.");
        }

        this.actualHarvestDate = actualHarvestDate;
        this.totalYieldQuantity = totalYieldQuantity;
        this.totalYieldUnitMasterId = totalYieldUnitMasterId;
        this.qualityGradeMasterId = qualityGradeMasterId;
    }

    public LocalDate getActualHarvestDate() {
        return actualHarvestDate;
    }

    public BigDecimal getTotalYieldQuantity() {
        return totalYieldQuantity;
    }

    public String getTotalYieldUnitMasterId() {
        return totalYieldUnitMasterId;
    }

    public String getQualityGradeMasterId() {
        return qualityGradeMasterId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HarvestInformation that = (HarvestInformation) o;
        return Objects.equals(actualHarvestDate, that.actualHarvestDate) &&
               Objects.equals(totalYieldQuantity, that.totalYieldQuantity) &&
               Objects.equals(totalYieldUnitMasterId, that.totalYieldUnitMasterId) &&
               Objects.equals(qualityGradeMasterId, that.qualityGradeMasterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actualHarvestDate, totalYieldQuantity, totalYieldUnitMasterId, qualityGradeMasterId);
    }
}