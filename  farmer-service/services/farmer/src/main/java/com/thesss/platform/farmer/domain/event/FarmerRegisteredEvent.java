package com.thesss.platform.farmer.domain.event;

import com.thesss.platform.farmer.domain.model.FarmerId;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain event published when a new farmer is successfully registered and approved.
 * REQ-FRM-001
 */
public record FarmerRegisteredEvent(FarmerId farmerId, LocalDateTime registrationTimestamp) {
    public FarmerRegisteredEvent {
        Objects.requireNonNull(farmerId, "FarmerId cannot be null for FarmerRegisteredEvent");
        Objects.requireNonNull(registrationTimestamp, "Registration timestamp cannot be null for FarmerRegisteredEvent");
    }
}