package com.thesss.platform.aggregators.event.dto.crop;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * DTO for crop cycle creation events.
 * Includes crop cycle ID, farmer ID, land ID, crop name, cultivated area, status.
 */
public record CropCycleCreatedEventDTO(
    String cropCycleId,
    String farmerId,
    String landId,
    String cropName,
    Double cultivatedArea,
    LocalDate sowingDate,
    String initialStatus, // e.g., "PLANTED", "ACTIVE"
    OffsetDateTime creationTimestamp
) {
}