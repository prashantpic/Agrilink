package com.thesss.platform.land.application.port.in;

import com.thesss.platform.land.application.dto.command.UpdateFarmLandRecordCommand;
import com.thesss.platform.land.domain.model.LandRecordId;

public interface UpdateFarmLandRecordUseCase { // REQ-2-001
    /**
     * Updates an existing farm land record.
     *
     * @param landRecordId The ID of the farm land record to update.
     * @param command      The command object containing the updated details.
     */
    void updateFarmLandRecord(LandRecordId landRecordId, UpdateFarmLandRecordCommand command);
}