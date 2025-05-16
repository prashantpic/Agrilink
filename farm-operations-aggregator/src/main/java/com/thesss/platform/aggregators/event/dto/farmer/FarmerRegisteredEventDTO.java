package com.thesss.platform.aggregators.event.dto.farmer;

import java.time.OffsetDateTime;

/**
 * DTO for farmer registration events.
 * Contains details of a newly registered farmer relevant for projections.
 */
public record FarmerRegisteredEventDTO(
    String farmerId,
    String firstName,
    String lastName,
    OffsetDateTime registrationTimestamp,
    // Example field that might contribute to completeness score, actual fields depend on source event
    boolean isProfileDataComplete, 
    String status
) {
}