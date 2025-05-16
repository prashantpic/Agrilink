package com.thesss.platform.crop.application.dtos;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class CropCycleDto {

    private UUID id;
    private UUID cropCycleBusinessId;
    private UUID farmerId;
    private UUID landRecordId;
    private String cropName;
    private String cropVariety;
    private String season;
    private Integer cultivationYear;
    private LocalDate plannedSowingDate;
    private LocalDate actualSowingDate;
    private LocalDate expectedHarvestDate;
    private LocalDate actualHarvestDate;
    private String seedingRate; // e.g., "50 kg/ha"
    private String seedSource;
    private String cultivatedArea; // e.g., "2.5 ha"
    private String status;
    private String reasonForFailure;
    private String totalYield; // e.g., "1200 kg"
    private String qualityGrade;
    private String yieldPerUnitArea; // e.g., "480 kg/ha"
    private String notes;
    private List<FarmingActivityDto> activities;
    private List<MarketSaleDto> marketSales;
    private Instant recordCreationDate;
    private Instant lastUpdatedDate;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCropCycleBusinessId() { return cropCycleBusinessId; }
    public void setCropCycleBusinessId(UUID cropCycleBusinessId) { this.cropCycleBusinessId = cropCycleBusinessId; }

    public UUID getFarmerId() { return farmerId; }
    public void setFarmerId(UUID farmerId) { this.farmerId = farmerId; }

    public UUID getLandRecordId() { return landRecordId; }
    public void setLandRecordId(UUID landRecordId) { this.landRecordId = landRecordId; }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }

    public String getCropVariety() { return cropVariety; }
    public void setCropVariety(String cropVariety) { this.cropVariety = cropVariety; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public Integer getCultivationYear() { return cultivationYear; }
    public void setCultivationYear(Integer cultivationYear) { this.cultivationYear = cultivationYear; }

    public LocalDate getPlannedSowingDate() { return plannedSowingDate; }
    public void setPlannedSowingDate(LocalDate plannedSowingDate) { this.plannedSowingDate = plannedSowingDate; }

    public LocalDate getActualSowingDate() { return actualSowingDate; }
    public void setActualSowingDate(LocalDate actualSowingDate) { this.actualSowingDate = actualSowingDate; }

    public LocalDate getExpectedHarvestDate() { return expectedHarvestDate; }
    public void setExpectedHarvestDate(LocalDate expectedHarvestDate) { this.expectedHarvestDate = expectedHarvestDate; }

    public LocalDate getActualHarvestDate() { return actualHarvestDate; }
    public void setActualHarvestDate(LocalDate actualHarvestDate) { this.actualHarvestDate = actualHarvestDate; }

    public String getSeedingRate() { return seedingRate; }
    public void setSeedingRate(String seedingRate) { this.seedingRate = seedingRate; }

    public String getSeedSource() { return seedSource; }
    public void setSeedSource(String seedSource) { this.seedSource = seedSource; }

    public String getCultivatedArea() { return cultivatedArea; }
    public void setCultivatedArea(String cultivatedArea) { this.cultivatedArea = cultivatedArea; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReasonForFailure() { return reasonForFailure; }
    public void setReasonForFailure(String reasonForFailure) { this.reasonForFailure = reasonForFailure; }

    public String getTotalYield() { return totalYield; }
    public void setTotalYield(String totalYield) { this.totalYield = totalYield; }

    public String getQualityGrade() { return qualityGrade; }
    public void setQualityGrade(String qualityGrade) { this.qualityGrade = qualityGrade; }

    public String getYieldPerUnitArea() { return yieldPerUnitArea; }
    public void setYieldPerUnitArea(String yieldPerUnitArea) { this.yieldPerUnitArea = yieldPerUnitArea; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<FarmingActivityDto> getActivities() { return activities; }
    public void setActivities(List<FarmingActivityDto> activities) { this.activities = activities; }

    public List<MarketSaleDto> getMarketSales() { return marketSales; }
    public void setMarketSales(List<MarketSaleDto> marketSales) { this.marketSales = marketSales; }

    public Instant getRecordCreationDate() { return recordCreationDate; }
    public void setRecordCreationDate(Instant recordCreationDate) { this.recordCreationDate = recordCreationDate; }

    public Instant getLastUpdatedDate() { return lastUpdatedDate; }
    public void setLastUpdatedDate(Instant lastUpdatedDate) { this.lastUpdatedDate = lastUpdatedDate; }
}