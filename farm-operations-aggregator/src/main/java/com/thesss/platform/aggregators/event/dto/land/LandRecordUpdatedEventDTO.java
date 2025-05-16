package com.thesss.platform.aggregators.event.dto.land;

import java.time.OffsetDateTime;

/**
 * DTO for land record update events.
 * Includes land ID and changed attributes like total area or GPS data status.
 */
public record LandRecordUpdatedEventDTO(
    String landId,
    String farmerId,
    Double newTotalArea, // The new total area after update
    boolean newHasGpsCoordinates, // The new GPS status after update
    OffsetDateTime updateTimestamp
) {
}