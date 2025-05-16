package com.thesss.platform.crop.application.dtos;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class FarmingActivityDto {

    private UUID id;
    private String activityType; // Resolved from MasterData
    private LocalDate activityDate;
    private String laborUsed; // e.g., "10 Man-days"
    private String notes;
    private List<InputUsageDto> inputsUsed;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public LocalDate getActivityDate() { return activityDate; }
    public void setActivityDate(LocalDate activityDate) { this.activityDate = activityDate; }

    public String getLaborUsed() { return laborUsed; }
    public void setLaborUsed(String laborUsed) { this.laborUsed = laborUsed; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<InputUsageDto> getInputsUsed() { return inputsUsed; }
    public void setInputsUsed(List<InputUsageDto> inputsUsed) { this.inputsUsed = inputsUsed; }
}