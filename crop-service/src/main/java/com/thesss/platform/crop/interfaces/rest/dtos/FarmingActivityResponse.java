package com.thesss.platform.crop.interfaces.rest.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FarmingActivityResponse {

    private UUID id;
    private String activityType;
    private LocalDate activityDate;
    private String laborUsed;
    private String notes;
    private List<InputUsageResponse> inputsUsed;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(LocalDate activityDate) {
        this.activityDate = activityDate;
    }

    public String getLaborUsed() {
        return laborUsed;
    }

    public void setLaborUsed(String laborUsed) {
        this.laborUsed = laborUsed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<InputUsageResponse> getInputsUsed() {
        return inputsUsed;
    }

    public void setInputsUsed(List<InputUsageResponse> inputsUsed) {
        this.inputsUsed = inputsUsed;
    }
}