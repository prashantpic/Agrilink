package com.thesss.platform.aggregators.event.dto.farmer;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * DTO for farmer profile update events.
 * Contains farmer ID and changed attributes relevant for projections.
 */
public record FarmerProfileUpdatedEventDTO(
    String farmerId,
    OffsetDateTime updateTimestamp,
    // Example field representing new completeness, actual fields depend on source event
    boolean isProfileDataComplete, 
    Map<String, Object> updatedFields // Can contain specific updated fields
) {
}