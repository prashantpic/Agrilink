package com.thesss.platform.land.application.port.out;

import com.thesss.platform.land.domain.model.FarmLandRecord;
import com.thesss.platform.land.domain.model.FarmerId;
import com.thesss.platform.land.domain.model.LandRecordId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface FarmLandRecordRepositoryPort { // REQ-2-001, REQ-2-004
    /**
     * Saves a farm land record.
     *
     * @param farmLandRecord The farm land record to save.
     * @return The saved farm land record.
     */
    FarmLandRecord save(FarmLandRecord farmLandRecord);

    /**
     * Finds a farm land record by its ID.
     *
     * @param id The ID of the farm land record.
     * @return An Optional containing the farm land record if found, otherwise empty.
     */
    Optional<FarmLandRecord> findById(LandRecordId id);

    /**
     * Finds all farm land records with pagination.
     *
     * @param pageable Pagination information.
     * @return A page of farm land records.
     */
    Page<FarmLandRecord> findAll(Pageable pageable);


    /**
     * Finds farm land records associated with a specific farmer, with pagination.
     *
     * @param farmerId The ID of the farmer.
     * @param pageable Pagination information.
     * @return A page of farm land records.
     */
    Page<FarmLandRecord> findByFarmerId(FarmerId farmerId, Pageable pageable);

    /**
     * Checks if a parcel ID already exists within a specified administrative region.
     *
     * @param parcelId The parcel ID to check.
     * @param region The administrative region code.
     * @return true if the parcel ID exists in the region, false otherwise.
     */
    boolean existsByParcelIdAndRegion(String parcelId, String region);

    // Add other necessary finder methods as required by the application service
}