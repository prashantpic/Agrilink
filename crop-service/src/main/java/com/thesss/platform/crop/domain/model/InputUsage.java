package com.thesss.platform.crop.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "input_usages")
public class InputUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farming_activity_id", nullable = false)
    private FarmingActivity farmingActivity; // Link back to parent entity

    @Column(name = "input_type_master_id", nullable = false)
    private String inputTypeMasterId; // References MasterData ID (e.g., FERTILIZER, PESTICIDE, WATER)

    @Column(name = "input_name_brand_master_id_or_text")
    private String inputNameBrandMasterIdOrText; // References MasterData ID or free text

    @Column(name = "quantity_value", precision = 12, scale = 3, nullable = false)
    private BigDecimal quantityValue;

    @Column(name = "quantity_unit_master_id", nullable = false)
    private String quantityUnitMasterId; // References MasterData ID for unit

    @Column(name = "application_method_master_id")
    private String applicationMethodMasterId; // Optional, references MasterData ID

    @Column(name = "cost", precision = 12, scale = 2) // Cost typically uses 2 decimal places
    private BigDecimal cost; // Optional

    // JPA requires a no-arg constructor
    protected InputUsage() {
    }

    public InputUsage(String inputTypeMasterId, String inputNameBrandMasterIdOrText,
                      BigDecimal quantityValue, String quantityUnitMasterId,
                      String applicationMethodMasterId, BigDecimal cost) {
        // Basic validation
        if (inputTypeMasterId == null || inputTypeMasterId.isBlank()) {
             throw new IllegalArgumentException("Input type master ID cannot be null or empty");
        }
         if (quantityValue == null || quantityValue.compareTo(BigDecimal.ZERO) < 0) {
             throw new IllegalArgumentException("Quantity value must be non-negative");
         }
         if (quantityUnitMasterId == null || quantityUnitMasterId.isBlank()) {
             throw new IllegalArgumentException("Quantity unit master ID cannot be null or empty");
         }
        if (cost != null && cost.compareTo(BigDecimal.ZERO) < 0) {
             throw new IllegalArgumentException("Cost must be non-negative if provided");
        }


        this.inputTypeMasterId = inputTypeMasterId;
        this.inputNameBrandMasterIdOrText = inputNameBrandMasterIdOrText;
        this.quantityValue = quantityValue;
        this.quantityUnitMasterId = quantityUnitMasterId;
        this.applicationMethodMasterId = applicationMethodMasterId;
        this.cost = cost;
    }

    // --- Getters and setters for relationships (used internally by aggregate/parent entity) ---

    public UUID getId() {
        return id;
    }

    // Setter for the relationship, used by the parent entity (FarmingActivity)
    void setFarmingActivity(FarmingActivity farmingActivity) {
        this.farmingActivity = farmingActivity;
    }

    public FarmingActivity getFarmingActivity() {
        return farmingActivity;
    }

    // --- Getters for properties ---

    public String getInputTypeMasterId() {
        return inputTypeMasterId;
    }

    public String getInputNameBrandMasterIdOrText() {
        return inputNameBrandMasterIdOrText;
    }

    public BigDecimal getQuantityValue() {
        return quantityValue;
    }

    public String getQuantityUnitMasterId() {
        return quantityUnitMasterId;
    }

    public String getApplicationMethodMasterId() {
        return applicationMethodMasterId;
    }

    public BigDecimal getCost() {
        return cost;
    }
}