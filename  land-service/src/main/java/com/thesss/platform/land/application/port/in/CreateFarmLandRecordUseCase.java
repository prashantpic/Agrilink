package com.thesss.platform.land.application.port.in;

import com.thesss.platform.land.application.dto.command.CreateFarmLandRecordCommand;
import com.thesss.platform.land.domain.model.LandRecordId;

public interface CreateFarmLandRecordUseCase { // REQ-2-001
    /**
     * Creates a new farm land record.
     *
     * @param command The command object containing the details of the farm land record to create.
     * @return The ID of the newly created farm land record.
     */
    LandRecordId createFarmLandRecord(CreateFarmLandRecordCommand command);
}