package com.thesss.platform.crop.domain.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import com.thesss.platform.crop.domain.exceptions.ValidationException;

import java.math.BigDecimal; // Assuming BigDecimal is for precision
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "crop_cycles")
@EntityListeners(AuditingEntityListener.class)
public class CropCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "crop_cycle_id", unique = true, nullable = false))
    })
    private CropCycleId cropCycleId; // Business ID

    @Column(name = "farmer_id", nullable = false)
    private UUID farmerId; // FK to Farmer Service

    @Column(name = "land_record_id", nullable = false)
    private UUID landRecordId; // FK to Land Service

    @Embedded
    private CropDetails cropDetails;

    @Embedded
    private SowingInformation sowingInformation;

    @Embedded
    private HarvestInformation harvestInformation; // Optional

    @Embedded
    private CultivationArea cultivatedArea;

    @Embedded
    private CropCycleStatusInfo statusInfo;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "cropCycle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FarmingActivity> activities = new ArrayList<>();

    @OneToMany(mappedBy = "cropCycle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MarketSale> marketSales = new ArrayList<>();

    @CreatedDate
    @Column(name = "record_creation_date", nullable = false, updatable = false)
    private Instant recordCreationDate;

    @LastModifiedDate
    @Column(name = "last_updated_date", nullable = false)
    private Instant lastUpdatedDate;

    protected CropCycle() {
    }

    public CropCycle(CropCycleId cropCycleId, UUID farmerId, UUID landRecordId, CropDetails cropDetails,
                     SowingInformation sowingInformation, CultivationArea cultivatedArea, CropCycleStatusInfo statusInfo, String notes) {
        this.cropCycleId = cropCycleId != null ? cropCycleId : new CropCycleId(UUID.randomUUID());
        this.farmerId = farmerId;
        this.landRecordId = landRecordId;
        this.cropDetails = cropDetails;
        this.sowingInformation = sowingInformation;
        this.cultivatedArea = cultivatedArea;
        this.statusInfo = statusInfo;
        this.notes = notes;
        validateCreation();
    }

    public void addFarmingActivity(FarmingActivity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity cannot be null");
        }
        // REQ-4-010: Actual sowing date must be set before adding activities. This should be checked by SM guard primarily.
        if (this.sowingInformation == null || this.sowingInformation.getActualSowingDate() == null) {
            throw new ValidationException("Cannot add farming activity before actual sowing date is set (REQ-4-010).");
        }
        activity.setCropCycle(this);
        this.activities.add(activity);
    }

    public void addMarketSale(MarketSale sale) {
         if (sale == null) {
             throw new IllegalArgumentException("Market sale cannot be null");
         }
         // Typically, sales are added after harvest. This could be a SM guard.
         sale.setCropCycle(this);
         this.marketSales.add(sale);
     }

    public void recordHarvestDetails(HarvestInformation harvestInfo) {
        if (harvestInfo == null || harvestInfo.getActualHarvestDate() == null || harvestInfo.getTotalYieldQuantity() == null || harvestInfo.getTotalYieldUnitMasterId() == null) {
            throw new ValidationException("Harvest details must include date, quantity, and unit.");
        }
        // REQ-4-009: Actual harvest date must be after actual sowing date.
        if (this.sowingInformation != null && this.sowingInformation.getActualSowingDate() != null && harvestInfo.getActualHarvestDate().isBefore(this.sowingInformation.getActualSowingDate())) {
             throw new ValidationException("Actual harvest date (" + harvestInfo.getActualHarvestDate() + ") cannot be before actual sowing date (" + this.sowingInformation.getActualSowingDate() + ") (REQ-4-009).");
        }
        this.harvestInformation = harvestInfo;
    }

    public void updateStatus(String newStatusMasterId, String reasonForFailure) {
        // This method is typically called by a State Machine action.
        this.statusInfo = new CropCycleStatusInfo(newStatusMasterId, reasonForFailure);
    }

    public void updateCoreInfo(CropDetails cropDetails, SowingInformation sowingInformation, CultivationArea cultivatedArea, String notes) {
        if (cropDetails != null) this.cropDetails = cropDetails;
        if (sowingInformation != null) this.sowingInformation = sowingInformation;
        if (cultivatedArea != null) this.cultivatedArea = cultivatedArea;
        if (notes != null) this.notes = notes;
        validateConsistency();
    }

    private void validateCreation() {
        if (this.farmerId == null || this.landRecordId == null || this.cropDetails == null || this.sowingInformation == null || this.cultivatedArea == null || this.statusInfo == null) {
            throw new ValidationException("CropCycle requires farmerId, landRecordId, cropDetails, sowingInformation, cultivatedArea, and initial status.");
        }
        if (this.sowingInformation.getActualSowingDate() == null) {
            throw new ValidationException("Actual sowing date must be provided at creation if status implies SOWN or later (REQ-4-004).");
        }
    }

    private void validateConsistency() {
        if (this.sowingInformation != null && this.sowingInformation.getActualSowingDate() != null &&
            this.harvestInformation != null && this.harvestInformation.getActualHarvestDate() != null &&
            this.harvestInformation.getActualHarvestDate().isBefore(this.sowingInformation.getActualSowingDate())) {
            throw new ValidationException("Harvest date cannot be before sowing date (REQ-4-009).");
        }
    }

    public UUID getId() {
        return id;
    }

    public CropCycleId getCropCycleId() {
        return cropCycleId;
    }

    public UUID getFarmerId() {
        return farmerId;
    }

    public UUID getLandRecordId() {
        return landRecordId;
    }

    public CropDetails getCropDetails() {
        return cropDetails;
    }

    public SowingInformation getSowingInformation() {
        return sowingInformation;
    }

    public HarvestInformation getHarvestInformation() {
        return harvestInformation;
    }

    public CultivationArea getCultivatedArea() {
        return cultivatedArea;
    }

    public CropCycleStatusInfo getStatusInfo() {
        return statusInfo;
    }

    public String getNotes() {
        return notes;
    }

    public List<FarmingActivity> getActivities() {
        return activities;
    }

    public List<MarketSale> getMarketSales() {
        return marketSales;
    }

    public Instant getRecordCreationDate() {
        return recordCreationDate;
    }

    public Instant getLastUpdatedDate() {
        return lastUpdatedDate;
    }
}