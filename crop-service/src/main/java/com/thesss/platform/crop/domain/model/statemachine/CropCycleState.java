package com.thesss.platform.crop.domain.model.statemachine;

public enum CropCycleState {
    PLANNED,                // Initial state after creation
    SOWN,                   // Seeds have been sown
    GROWING,                // Active growth phase
    FLOWERING,              // Flowering stage (optional, crop-dependent)
    FRUITING,               // Fruiting/grain filling stage (optional, crop-dependent)
    READY_FOR_HARVEST,      // Crop is mature and ready for harvest
    HARVESTED,              // Crop has been harvested, yield recorded
    FAILED,                 // Crop cycle failed (e.g., due to drought, pests) - Terminal
    COMPLETED               // Crop cycle successfully completed (post-harvest activities done) - Terminal
}