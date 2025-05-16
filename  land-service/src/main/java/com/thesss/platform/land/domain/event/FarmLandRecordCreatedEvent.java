package com.thesss.platform.land.domain.event;

import com.thesss.platform.land.domain.model.FarmerId;
import com.thesss.platform.land.domain.model.LandRecordId;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a new farm land record is successfully created.
 * REQ-2-001
 */
@Value // Lombok annotation for immutable class
public class FarmLandRecordCreatedEvent {
    UUID eventId;
    Instant occurredOn;
    LandRecordId landRecordId;
    FarmerId farmerId;
    // Potentially other key attributes from the newly created record if useful for consumers,
    // but keeping events lightweight is often preferred.

    public FarmLandRecordCreatedEvent(LandRecordId landRecordId, FarmerId farmerId) {
        if (landRecordId == null) {
            throw new IllegalArgumentException("LandRecordId cannot be null for FarmLandRecordCreatedEvent.");
        }
        if (farmerId == null) {
            throw new IllegalArgumentException("FarmerId cannot be null for FarmLandRecordCreatedEvent.");
        }
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
        this.landRecordId = landRecordId;
        this.farmerId = farmerId;
    }
}