package com.thesss.platform.land.domain.repository;

import com.thesss.platform.land.domain.model.FarmLandRecord;
import com.thesss.platform.land.domain.model.FarmerId;
import com.thesss.platform.land.domain.model.LandRecordId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Domain repository interface defining persistence operations for FarmLandRecord aggregates.
 * REQ-2-001, REQ-2-004
 */
public interface FarmLandRecordRepository {

    /**
     * Saves a farm land record aggregate. This can be for creation or update.
     *
     * @param farmLandRecord The aggregate to save.
     * @return The saved aggregate, potentially with updated state (e.g., version, audit info).
     */
    FarmLandRecord save(FarmLandRecord farmLandRecord);

    /**
     * Finds a farm land record aggregate by its unique identifier.
     *
     * @param id The ID of the aggregate.
     * @return An Optional containing the aggregate if found, otherwise empty.
     */
    Optional<FarmLandRecord> findById(LandRecordId id);

    /**
     * Finds all farm land records, paginated.
     * This is a generic finder; more specific finders might be needed.
     *
     * @param pageable Pagination information.
     * @return A page of farm land record aggregates.
     */
    Page<FarmLandRecord> findAll(Pageable pageable);

    /**
     * Finds farm land records associated with a specific farmer, paginated.
     *
     * @param farmerId The ID of the farmer.
     * @param pageable Pagination information.
     * @return A page of farm land record aggregates.
     */
    Page<FarmLandRecord> findByFarmerId(FarmerId farmerId, Pageable pageable);

    /**
     * Checks if a farm land record with the given parcel ID already exists within the specified region.
     * This supports the business rule for parcel ID uniqueness.
     *
     * @param parcelId The parcel ID to check.
     * @param region   The administrative region code for uniqueness check.
     * @return true if a record with the parcel ID exists in the region, false otherwise.
     */
    boolean existsByParcelIdAndRegion(String parcelId, String region);

    // Potentially other domain-specific query methods:
    // e.g., Optional<FarmLandRecord> findByParcelIdAndRegion(String parcelId, String region);
    // e.g., List<FarmLandRecord> findByStatus(LandRecordStatus status);
}