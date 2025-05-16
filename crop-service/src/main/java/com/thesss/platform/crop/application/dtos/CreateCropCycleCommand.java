package com.thesss.platform.crop.application.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class CreateCropCycleCommand {

    @NotNull(message = "Farmer ID cannot be null")
    private UUID farmerId;

    @NotNull(message = "Land Record ID cannot be null")
    private UUID landRecordId;

    @NotNull(message = "Crop Name Master ID cannot be null")
    @Size(min = 1, max = 255, message = "Crop Name Master ID must be between 1 and 255 characters")
    private String cropNameMasterId;

    @Size(max = 255, message = "Crop Variety must be up to 255 characters")
    private String cropVarietyMasterIdOrText;

    @NotNull(message = "Season Master ID cannot be null")
    @Size(min = 1, max = 255, message = "Season Master ID must be between 1 and 255 characters")
    private String seasonMasterId;

    @NotNull(message = "Cultivation year cannot be null")
    @Positive(message = "Cultivation year must be a positive integer")
    private Integer cultivationYear;

    private LocalDate plannedSowingDate; // Optional at creation, can be future

    @NotNull(message = "Actual sowing date cannot be null")
    @PastOrPresent(message = "Actual sowing date must be in the past or present")
    private LocalDate actualSowingDate;

    @PositiveOrZero(message = "Seeding rate value must be zero or positive")
    private BigDecimal seedingRateValue;

    @Size(max = 255, message = "Seeding rate unit must be up to 255 characters")
    private String seedingRateUnitMasterId; // Required if seedingRateValue is provided and > 0

    @Size(max = 255, message = "Seed source must be up to 255 characters")
    private String seedSourceMasterIdOrText;

    @NotNull(message = "Cultivated area value cannot be null")
    @Positive(message = "Cultivated area value must be positive")
    private BigDecimal cultivatedAreaValue;

    @NotNull(message = "Cultivated area unit Master ID cannot be null")
    @Size(min = 1, max = 255, message = "Cultivated area unit Master ID must be between 1 and 255 characters")
    private String cultivatedAreaUnitMasterId;

    @Size(max = 1000, message = "Notes must be up to 1000 characters")
    private String notes;

    // Getters and Setters
    public UUID getFarmerId() { return farmerId; }
    public void setFarmerId(UUID farmerId) { this.farmerId = farmerId; }

    public UUID getLandRecordId() { return landRecordId; }
    public void setLandRecordId(UUID landRecordId) { this.landRecordId = landRecordId; }

    public String getCropNameMasterId() { return cropNameMasterId; }
    public void setCropNameMasterId(String cropNameMasterId) { this.cropNameMasterId = cropNameMasterId; }

    public String getCropVarietyMasterIdOrText() { return cropVarietyMasterIdOrText; }
    public void setCropVarietyMasterIdOrText(String cropVarietyMasterIdOrText) { this.cropVarietyMasterIdOrText = cropVarietyMasterIdOrText; }

    public String getSeasonMasterId() { return seasonMasterId; }
    public void setSeasonMasterId(String seasonMasterId) { this.seasonMasterId = seasonMasterId; }

    public Integer getCultivationYear() { return cultivationYear; }
    public void setCultivationYear(Integer cultivationYear) { this.cultivationYear = cultivationYear; }

    public LocalDate getPlannedSowingDate() { return plannedSowingDate; }
    public void setPlannedSowingDate(LocalDate plannedSowingDate) { this.plannedSowingDate = plannedSowingDate; }

    public LocalDate getActualSowingDate() { return actualSowingDate; }
    public void setActualSowingDate(LocalDate actualSowingDate) { this.actualSowingDate = actualSowingDate; }

    public BigDecimal getSeedingRateValue() { return seedingRateValue; }
    public void setSeedingRateValue(BigDecimal seedingRateValue) { this.seedingRateValue = seedingRateValue; }

    public String getSeedingRateUnitMasterId() { return seedingRateUnitMasterId; }
    public void setSeedingRateUnitMasterId(String seedingRateUnitMasterId) { this.seedingRateUnitMasterId = seedingRateUnitMasterId; }

    public String getSeedSourceMasterIdOrText() { return seedSourceMasterIdOrText; }
    public void setSeedSourceMasterIdOrText(String seedSourceMasterIdOrText) { this.seedSourceMasterIdOrText = seedSourceMasterIdOrText; }

    public BigDecimal getCultivatedAreaValue() { return cultivatedAreaValue; }
    public void setCultivatedAreaValue(BigDecimal cultivatedAreaValue) { this.cultivatedAreaValue = cultivatedAreaValue; }

    public String getCultivatedAreaUnitMasterId() { return cultivatedAreaUnitMasterId; }
    public void setCultivatedAreaUnitMasterId(String cultivatedAreaUnitMasterId) { this.cultivatedAreaUnitMasterId = cultivatedAreaUnitMasterId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}