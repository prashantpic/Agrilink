package com.thesss.platform.crop.application.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AddFarmingActivityCommand {

    @NotNull(message = "Activity Type Master ID cannot be null")
    @Size(min = 1, max = 255, message = "Activity Type Master ID must be between 1 and 255 characters")
    private String activityTypeMasterId;

    @NotNull(message = "Activity date cannot be null")
    @PastOrPresent(message = "Activity date must be in the past or present")
    private LocalDate activityDate;

    @PositiveOrZero(message = "Labor used value must be zero or positive")
    private BigDecimal laborUsedValue;

    @Size(max = 255, message = "Labor used unit Master ID must be up to 255 characters")
    private String laborUsedUnitMasterId; // Required if laborUsedValue is provided and > 0

    @Size(max = 1000, message = "Notes must be up to 1000 characters")
    private String notes;

    // Getters and Setters
    public String getActivityTypeMasterId() { return activityTypeMasterId; }
    public void setActivityTypeMasterId(String activityTypeMasterId) { this.activityTypeMasterId = activityTypeMasterId; }

    public LocalDate getActivityDate() { return activityDate; }
    public void setActivityDate(LocalDate activityDate) { this.activityDate = activityDate; }

    public BigDecimal getLaborUsedValue() { return laborUsedValue; }
    public void setLaborUsedValue(BigDecimal laborUsedValue) { this.laborUsedValue = laborUsedValue; }

    public String getLaborUsedUnitMasterId() { return laborUsedUnitMasterId; }
    public void setLaborUsedUnitMasterId(String laborUsedUnitMasterId) { this.laborUsedUnitMasterId = laborUsedUnitMasterId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}