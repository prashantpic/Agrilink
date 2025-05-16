package com.thesss.platform.crop.interfaces.rest.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class LogInputUsageRequest {

    @NotNull(message = "Input Type Master ID cannot be null.")
    @Size(min = 1, max = 255, message = "Input Type Master ID must be between 1 and 255 characters.")
    private String inputTypeMasterId;

    @Size(max = 255, message = "Input Name/Brand must be no more than 255 characters.")
    private String inputNameBrandMasterIdOrText; // Optional

    @NotNull(message = "Quantity value cannot be null.")
    @Positive(message = "Quantity value must be positive.")
    private BigDecimal quantityValue;

    @NotNull(message = "Quantity Unit Master ID cannot be null.")
    @Size(min = 1, max = 255, message = "Quantity Unit Master ID must be between 1 and 255 characters.")
    private String quantityUnitMasterId;

    @Size(max = 255, message = "Application Method Master ID must be no more than 255 characters.")
    private String applicationMethodMasterId; // Optional

    @PositiveOrZero(message = "Cost must be positive or zero.")
    private BigDecimal cost; // Optional

    // Getters and Setters
    public String getInputTypeMasterId() {
        return inputTypeMasterId;
    }

    public void setInputTypeMasterId(String inputTypeMasterId) {
        this.inputTypeMasterId = inputTypeMasterId;
    }

    public String getInputNameBrandMasterIdOrText() {
        return inputNameBrandMasterIdOrText;
    }

    public void setInputNameBrandMasterIdOrText(String inputNameBrandMasterIdOrText) {
        this.inputNameBrandMasterIdOrText = inputNameBrandMasterIdOrText;
    }

    public BigDecimal getQuantityValue() {
        return quantityValue;
    }

    public void setQuantityValue(BigDecimal quantityValue) {
        this.quantityValue = quantityValue;
    }

    public String getQuantityUnitMasterId() {
        return quantityUnitMasterId;
    }

    public void setQuantityUnitMasterId(String quantityUnitMasterId) {
        this.quantityUnitMasterId = quantityUnitMasterId;
    }

    public String getApplicationMethodMasterId() {
        return applicationMethodMasterId;
    }

    public void setApplicationMethodMasterId(String applicationMethodMasterId) {
        this.applicationMethodMasterId = applicationMethodMasterId;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }
}