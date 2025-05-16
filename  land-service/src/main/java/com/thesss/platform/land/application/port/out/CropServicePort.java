package com.thesss.platform.land.application.port.out;

import com.thesss.platform.land.domain.model.LandRecordId;

// This is a placeholder interface definition.
// Implementations will interact with the Crop Service via HTTP.
public interface CropServicePort { // REQ-2-017

    /**
     * Fetches a summary of crop cultivation history for a specific land record.
     *
     * @param landRecordId The ID of the land record.
     * @return A DTO representing the crop history summary for this land.
     *         Define a DTO like CropHistorySummary in a shared or client library.
     */
    // CropHistorySummary getCropHistorySummaryForLand(LandRecordId landRecordId); // Example method signature and return type
}