package com.thesss.platform.crop.interfaces.rest.dtos;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateCropCycleCoreInfoRequest {

    @Size(max = 255, message = "Crop Name Master ID must be no more than 255 characters.")
    private String cropNameMasterId; // Optional

    @Size(max = 255, message = "Crop Variety must be no more than 255 characters.")
    private String cropVarietyMasterIdOrText; // Optional

    @Size(max = 255, message = "Season Master ID must be no more than 255 characters.")
    private String seasonMasterId; // Optional

    @Positive(message = "Cultivation year must be a positive integer if provided.")
    private Integer cultivationYear; // Optional

    private LocalDate plannedSowingDate; // Optional

    @PastOrPresent(message = "Actual sowing date must be in the past or present if provided.")
    private LocalDate actualSowingDate; // Optional

    @FutureOrPresent(message = "Expected harvest date must be in the present or future if provided.")
    private LocalDate expectedHarvestDate; // Optional, user-override

    @PositiveOrZero(message = "Seeding rate value must be positive or zero if provided.")
    private BigDecimal seedingRateValue; // Optional

    @Size(max = 255, message = "Seeding Rate Unit Master ID must be no more than 255 characters.")
    private String seedingRateUnitMasterId; // Optional

    @Size(max = 255, message = "Seed Source must be no more than 255 characters.")
    private String seedSourceMasterIdOrText; // Optional

    @Positive(message = "Cultivated area value must be positive if provided.")
    private BigDecimal cultivatedAreaValue; // Optional

    @Size(max = 255, message = "Cultivated Area Unit Master ID must be no more than 255 characters.")
    private String cultivatedAreaUnitMasterId; // Optional

    @Size(max = 1000, message = "Notes must be no more than 1000 characters.")
    private String notes; // Optional

    // Getters and Setters
    public String getCropNameMasterId() {
        return cropNameMasterId;
    }

    public void setCropNameMasterId(String cropNameMasterId) {
        this.cropNameMasterId = cropNameMasterId;
    }

    public String getCropVarietyMasterIdOrText() {
        return cropVarietyMasterIdOrText;
    }

    public void setCropVarietyMasterIdOrText(String cropVarietyMasterIdOrText) {
        this.cropVarietyMasterIdOrText = cropVarietyMasterIdOrText;
    }

    public String getSeasonMasterId() {
        return seasonMasterId;
    }

    public void setSeasonMasterId(String seasonMasterId) {
        this.seasonMasterId = seasonMasterId;
    }

    public Integer getCultivationYear() {
        return cultivationYear;
    }

    public void setCultivationYear(Integer cultivationYear) {
        this.cultivationYear = cultivationYear;
    }

    public LocalDate getPlannedSowingDate() {
        return plannedSowingDate;
    }

    public void setPlannedSowingDate(LocalDate plannedSowingDate) {
        this.plannedSowingDate = plannedSowingDate;
    }

    public LocalDate getActualSowingDate() {
        return actualSowingDate;
    }

    public void setActualSowingDate(LocalDate actualSowingDate) {
        this.actualSowingDate = actualSowingDate;
    }

    public LocalDate getExpectedHarvestDate() {
        return expectedHarvestDate;
    }

    public void setExpectedHarvestDate(LocalDate expectedHarvestDate) {
        this.expectedHarvestDate = expectedHarvestDate;
    }

    public BigDecimal getSeedingRateValue() {
        return seedingRateValue;
    }

    public void setSeedingRateValue(BigDecimal seedingRateValue) {
        this.seedingRateValue = seedingRateValue;
    }

    public String getSeedingRateUnitMasterId() {
        return seedingRateUnitMasterId;
    }

    public void setSeedingRateUnitMasterId(String seedingRateUnitMasterId) {
        this.seedingRateUnitMasterId = seedingRateUnitMasterId;
    }

    public String getSeedSourceMasterIdOrText() {
        return seedSourceMasterIdOrText;
    }

    public void setSeedSourceMasterIdOrText(String seedSourceMasterIdOrText) {
        this.seedSourceMasterIdOrText = seedSourceMasterIdOrText;
    }

    public BigDecimal getCultivatedAreaValue() {
        return cultivatedAreaValue;
    }

    public void setCultivatedAreaValue(BigDecimal cultivatedAreaValue) {
        this.cultivatedAreaValue = cultivatedAreaValue;
    }

    public String getCultivatedAreaUnitMasterId() {
        return cultivatedAreaUnitMasterId;
    }

    public void setCultivatedAreaUnitMasterId(String cultivatedAreaUnitMasterId) {
        this.cultivatedAreaUnitMasterId = cultivatedAreaUnitMasterId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}