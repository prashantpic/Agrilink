package com.thesss.platform.land.application.port.in;

import com.thesss.platform.land.domain.model.LandRecordId;

public interface ManageLandRecordStatusUseCase { // REQ-2-019, REQ-2-021
    /**
     * Updates the status of a farm land record and potentially triggers an approval workflow.
     *
     * @param landRecordId The ID of the farm land record.
     * @param status       The new status code (String) for the land record.
     * @param reason       An optional reason for the status change.
     * @throws com.thesss.platform.land.domain.exception.FarmLandRecordNotFoundException If the land record is not found.
     * @throws IllegalArgumentException If the provided status code is invalid.
     */
    void updateLandRecordStatus(LandRecordId landRecordId, String status, String reason);
}