package com.thesss.platform.crop.application.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RecordHarvestCommand {

    @NotNull(message = "Actual harvest date cannot be null")
    @PastOrPresent(message = "Actual harvest date must be in the past or present")
    private LocalDate actualHarvestDate;

    @NotNull(message = "Total yield quantity cannot be null")
    @PositiveOrZero(message = "Total yield quantity must be zero or positive")
    private BigDecimal totalYieldQuantity;

    @NotNull(message = "Total yield unit Master ID cannot be null")
    @Size(min = 1, max = 255, message = "Total yield unit Master ID must be between 1 and 255 characters")
    private String totalYieldUnitMasterId;

    @Size(max = 255, message = "Quality grade Master ID must be up to 255 characters")
    private String qualityGradeMasterId;

    // Getters and Setters
    public LocalDate getActualHarvestDate() { return actualHarvestDate; }
    public void setActualHarvestDate(LocalDate actualHarvestDate) { this.actualHarvestDate = actualHarvestDate; }

    public BigDecimal getTotalYieldQuantity() { return totalYieldQuantity; }
    public void setTotalYieldQuantity(BigDecimal totalYieldQuantity) { this.totalYieldQuantity = totalYieldQuantity; }

    public String getTotalYieldUnitMasterId() { return totalYieldUnitMasterId; }
    public void setTotalYieldUnitMasterId(String totalYieldUnitMasterId) { this.totalYieldUnitMasterId = totalYieldUnitMasterId; }

    public String getQualityGradeMasterId() { return qualityGradeMasterId; }
    public void setQualityGradeMasterId(String qualityGradeMasterId) { this.qualityGradeMasterId = qualityGradeMasterId; }
}