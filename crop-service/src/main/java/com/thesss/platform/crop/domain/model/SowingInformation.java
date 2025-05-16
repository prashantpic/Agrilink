package com.thesss.platform.crop.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class SowingInformation {

    @Column(name = "planned_sowing_date")
    private LocalDate plannedSowingDate;

    @Column(name = "actual_sowing_date", nullable = false)
    private LocalDate actualSowingDate;

    @Column(name = "expected_harvest_date")
    private LocalDate expectedHarvestDate; // Can be calculated or user-overridden

    @Column(name = "seeding_rate_value", precision = 12, scale = 3)
    private BigDecimal seedingRateValue;

    @Column(name = "seeding_rate_unit_master_id")
    private String seedingRateUnitMasterId; // References MasterData ID for unit

    @Column(name = "seed_source_master_id_or_text")
    private String seedSourceMasterIdOrText; // References MasterData ID or free text

    protected SowingInformation() {
        // JPA LOMBOK Requirement
    }

    public SowingInformation(LocalDate plannedSowingDate, LocalDate actualSowingDate, LocalDate expectedHarvestDate,
                             BigDecimal seedingRateValue, String seedingRateUnitMasterId, String seedSourceMasterIdOrText) {
        if (actualSowingDate == null) {
            throw new IllegalArgumentException("Actual sowing date cannot be null");
        }
        if (seedingRateValue != null && seedingRateValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Seeding rate value cannot be negative");
        }
        if (seedingRateValue != null && (seedingRateUnitMasterId == null || seedingRateUnitMasterId.isBlank())) {
            throw new IllegalArgumentException("Seeding rate unit must be provided if seeding rate value is present");
        }
        this.plannedSowingDate = plannedSowingDate;
        this.actualSowingDate = actualSowingDate;
        this.expectedHarvestDate = expectedHarvestDate;
        this.seedingRateValue = seedingRateValue;
        this.seedingRateUnitMasterId = seedingRateUnitMasterId;
        this.seedSourceMasterIdOrText = seedSourceMasterIdOrText;
    }

    public SowingInformation updateExpectedHarvestDate(LocalDate newExpectedHarvestDate) {
        return new SowingInformation(
            this.plannedSowingDate,
            this.actualSowingDate,
            newExpectedHarvestDate,
            this.seedingRateValue,
            this.seedingRateUnitMasterId,
            this.seedSourceMasterIdOrText
        );
    }


    public LocalDate getPlannedSowingDate() {
        return plannedSowingDate;
    }

    public LocalDate getActualSowingDate() {
        return actualSowingDate;
    }

    public LocalDate getExpectedHarvestDate() {
        return expectedHarvestDate;
    }

    public BigDecimal getSeedingRateValue() {
        return seedingRateValue;
    }

    public String getSeedingRateUnitMasterId() {
        return seedingRateUnitMasterId;
    }

    public String getSeedSourceMasterIdOrText() {
        return seedSourceMasterIdOrText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SowingInformation that = (SowingInformation) o;
        return Objects.equals(plannedSowingDate, that.plannedSowingDate) &&
               Objects.equals(actualSowingDate, that.actualSowingDate) &&
               Objects.equals(expectedHarvestDate, that.expectedHarvestDate) &&
               Objects.equals(seedingRateValue, that.seedingRateValue) &&
               Objects.equals(seedingRateUnitMasterId, that.seedingRateUnitMasterId) &&
               Objects.equals(seedSourceMasterIdOrText, that.seedSourceMasterIdOrText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plannedSowingDate, actualSowingDate, expectedHarvestDate, seedingRateValue, seedingRateUnitMasterId, seedSourceMasterIdOrText);
    }
}