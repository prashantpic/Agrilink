package com.thesss.platform.crop.domain.model.statemachine;

public enum CropCycleEvent {
    START_SOWING,           // Triggered when actual sowing date is set or sowing process begins
    COMPLETE_SOWING,        // Triggered when sowing is finished, moves to growing stage
    PROGRESS,               // Generic event for automatic stage progression (e.g., growing to flowering based on time/conditions)
    FLAG_READY_FOR_HARVEST, // Manually or automatically flag that crop is ready for harvest
    START_HARVEST,          // Triggered when harvest activities begin / harvest details are recorded
    COMPLETE_HARVEST,       // All harvest activities are done (might be same as START_HARVEST if single event)
    RECORD_FAILURE,         // Triggered to mark the crop cycle as failed
    COMPLETE_CYCLE          // Triggered to mark a harvested or failed cycle as fully completed (e.g., all records finalized)
}