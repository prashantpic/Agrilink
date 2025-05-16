package com.thesss.platform.crop.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "farming_activities")
public class FarmingActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_cycle_id", nullable = false)
    private CropCycle cropCycle;

    @Column(name = "activity_type_master_id", nullable = false)
    private String activityTypeMasterId; // References MasterData ID

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "labor_used_value", precision = 12, scale = 3)
    private BigDecimal laborUsedValue; // Optional

    @Column(name = "labor_used_unit_master_id")
    private String laborUsedUnitMasterId; // Optional, references MasterData ID

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes; // Optional

    @OneToMany(mappedBy = "farmingActivity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InputUsage> inputsUsed = new ArrayList<>();

    protected FarmingActivity() {
        // JPA LOMBOK Requirement
    }

    public FarmingActivity(CropCycle cropCycle, String activityTypeMasterId, LocalDate activityDate,
                           BigDecimal laborUsedValue, String laborUsedUnitMasterId, String notes) {
        if (cropCycle == null) {
            throw new IllegalArgumentException("CropCycle cannot be null for a FarmingActivity");
        }
        if (activityTypeMasterId == null || activityTypeMasterId.isBlank()) {
            throw new IllegalArgumentException("Activity type master ID cannot be null or empty");
        }
        if (activityDate == null) {
            throw new IllegalArgumentException("Activity date cannot be null");
        }
        if (laborUsedValue != null && laborUsedValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Labor used value cannot be negative");
        }
        if (laborUsedValue != null && (laborUsedUnitMasterId == null || laborUsedUnitMasterId.isBlank())) {
            throw new IllegalArgumentException("Labor unit must be provided if labor value is present");
        }

        this.cropCycle = cropCycle;
        this.activityTypeMasterId = activityTypeMasterId;
        this.activityDate = activityDate;
        this.laborUsedValue = laborUsedValue;
        this.laborUsedUnitMasterId = laborUsedUnitMasterId;
        this.notes = notes;
    }

    public void addInputUsage(InputUsage inputUsage) {
        if (inputUsage == null) {
            throw new IllegalArgumentException("Input usage cannot be null");
        }
        this.inputsUsed.add(inputUsage);
        inputUsage.setFarmingActivity(this); // Set back-reference
    }

    public void removeInputUsage(InputUsage inputUsage) {
        if (inputUsage == null) {
            return;
        }
        this.inputsUsed.remove(inputUsage);
        inputUsage.setFarmingActivity(null);
    }


    public UUID getId() {
        return id;
    }

    public CropCycle getCropCycle() {
        return cropCycle;
    }

    // Package-private setter for aggregate root to manage relationship
    void setCropCycle(CropCycle cropCycle) {
        this.cropCycle = cropCycle;
    }

    public String getActivityTypeMasterId() {
        return activityTypeMasterId;
    }

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public BigDecimal getLaborUsedValue() {
        return laborUsedValue;
    }

    public String getLaborUsedUnitMasterId() {
        return laborUsedUnitMasterId;
    }

    public String getNotes() {
        return notes;
    }

    public List<InputUsage> getInputsUsed() {
        // Return a copy to prevent external modification of the list if desired,
        // but for JPA lazy loading, returning the direct reference is common.
        return inputsUsed;
    }
}