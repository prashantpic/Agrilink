package com.thesss.platform.aggregators.event.dto.land;

import java.time.OffsetDateTime;

/**
 * DTO for land record creation events.
 * Includes land ID, farmer ID, total area, GPS data presence, etc.
 */
public record LandRecordCreatedEventDTO(
    String landId,
    String farmerId,
    Double totalArea,
    boolean hasGpsCoordinates,
    OffsetDateTime creationTimestamp
) {
}