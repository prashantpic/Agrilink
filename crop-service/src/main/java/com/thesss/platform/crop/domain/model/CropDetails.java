package com.thesss.platform.crop.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import java.util.Objects;

@Embeddable
public class CropDetails {

    @Column(name = "crop_name_master_id", nullable = false)
    private String cropNameMasterId; // References MasterData ID for crop name

    @Column(name = "crop_variety_master_id_or_text")
    private String cropVarietyMasterIdOrText; // References MasterData ID for variety or free text

    @Column(name = "season_master_id", nullable = false)
    private String seasonMasterId; // References MasterData ID for season

    @Column(name = "cultivation_year", nullable = false)
    private Integer cultivationYear;

    protected CropDetails() {
        // JPA LOMBOK Requirement
    }

    public CropDetails(String cropNameMasterId, String cropVarietyMasterIdOrText, String seasonMasterId, Integer cultivationYear) {
        if (cropNameMasterId == null || cropNameMasterId.isBlank()) {
            throw new IllegalArgumentException("Crop name master ID cannot be null or empty");
        }
        if (seasonMasterId == null || seasonMasterId.isBlank()) {
            throw new IllegalArgumentException("Season master ID cannot be null or empty");
        }
        if (cultivationYear == null || cultivationYear <= 1900 || cultivationYear > 2200) { // Basic sanity check
            throw new IllegalArgumentException("Cultivation year must be a valid year");
        }
        this.cropNameMasterId = cropNameMasterId;
        this.cropVarietyMasterIdOrText = cropVarietyMasterIdOrText;
        this.seasonMasterId = seasonMasterId;
        this.cultivationYear = cultivationYear;
    }

    public String getCropNameMasterId() {
        return cropNameMasterId;
    }

    public String getCropVarietyMasterIdOrText() {
        return cropVarietyMasterIdOrText;
    }

    public String getSeasonMasterId() {
        return seasonMasterId;
    }

    public Integer getCultivationYear() {
        return cultivationYear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CropDetails that = (CropDetails) o;
        return Objects.equals(cropNameMasterId, that.cropNameMasterId) &&
               Objects.equals(cropVarietyMasterIdOrText, that.cropVarietyMasterIdOrText) &&
               Objects.equals(seasonMasterId, that.seasonMasterId) &&
               Objects.equals(cultivationYear, that.cultivationYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cropNameMasterId, cropVarietyMasterIdOrText, seasonMasterId, cultivationYear);
    }
}