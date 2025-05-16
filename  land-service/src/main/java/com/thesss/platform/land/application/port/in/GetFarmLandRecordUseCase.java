package com.thesss.platform.land.application.port.in;

import com.thesss.platform.land.application.dto.query.FarmLandRecordQuery;
import com.thesss.platform.land.domain.model.FarmerId;
import com.thesss.platform.land.domain.model.LandRecordId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetFarmLandRecordUseCase { // REQ-2-001, REQ-2-003, REQ-2-017

    /**
     * Retrieves a farm land record by its unique identifier.
     *
     * @param landRecordId The ID of the farm land record.
     * @return The farm land record query object.
     * @throws com.thesss.platform.land.domain.exception.FarmLandRecordNotFoundException if the record is not found.
     */
    FarmLandRecordQuery getFarmLandRecordById(LandRecordId landRecordId);

    /**
     * Retrieves all farm land records with pagination.
     *
     * @param pageable Pagination information.
     * @return A page of farm land record query objects.
     */
    Page<FarmLandRecordQuery> getAllFarmLandRecords(Pageable pageable);

    /**
     * Retrieves farm land records associated with a specific farmer, with pagination.
     *
     * @param farmerId The ID of the farmer.
     * @param pageable Pagination information.
     * @return A page of farm land record query objects.
     */
    Page<FarmLandRecordQuery> getFarmLandRecordsByFarmerId(FarmerId farmerId, Pageable pageable);
}