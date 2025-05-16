package com.thesss.platform.land.domain.event;

import com.thesss.platform.land.domain.model.FarmerId;
import com.thesss.platform.land.domain.model.LandRecordId;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;
// import java.util.Set; // If including changed fields

/**
 * Domain event published when a farm land record is successfully updated.
 * REQ-2-001
 */
@Value // Lombok annotation for immutable class
public class FarmLandRecordUpdatedEvent {
    UUID eventId;
    Instant occurredOn;
    LandRecordId landRecordId;
    FarmerId farmerId; // Include farmerId for context
    // Optional: A set of strings indicating which high-level aspects of the record were updated.
    // e.g., "DETAILS_CHANGED", "BOUNDARY_CHANGED", "STATUS_CHANGED", "DOCUMENTS_CHANGED"
    // Set<String> updatedAspects;

    public FarmLandRecordUpdatedEvent(LandRecordId landRecordId, FarmerId farmerId /*, Set<String> updatedAspects */) {
        if (landRecordId == null) {
            throw new IllegalArgumentException("LandRecordId cannot be null for FarmLandRecordUpdatedEvent.");
        }
        if (farmerId == null) {
            throw new IllegalArgumentException("FarmerId cannot be null for FarmLandRecordUpdatedEvent.");
        }
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.landRecordId = landRecordId;
        this.farmerId = farmerId;
        // this.updatedAspects = updatedAspects != null ? Set.copyOf(updatedAspects) : Set.of();
    }
}