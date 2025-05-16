package com.thesss.platform.crop.interfaces.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class ProcessCropCycleEventRequest {

    @NotNull(message = "Event name cannot be null.")
    @NotBlank(message = "Event name cannot be blank.")
    private String event; // Name of CropCycleEvent enum value

    private Map<String, Object> context; // Optional context data for the event/transition

    // Getters and Setters
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }
}