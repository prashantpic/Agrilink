package com.thesss.platform.aggregators.event.dto.crop;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * DTO for harvest recorded events.
 * Includes crop cycle ID, total yield, cultivated area (for verification/yield calculation), harvest date.
 */
public record HarvestRecordedEventDTO(
    String cropCycleId,
    String farmerId, // For context, might be redundant if cropCycleId is globally unique and links to farmer
    Double totalYield, // Quantity of harvest
    Double cultivatedArea, // Area from which harvest was made
    LocalDate harvestDate,
    OffsetDateTime eventTimestamp
) {
}