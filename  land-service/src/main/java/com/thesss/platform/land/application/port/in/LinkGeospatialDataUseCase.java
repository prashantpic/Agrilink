package com.thesss.platform.land.application.port.in;

import com.thesss.platform.land.application.dto.command.AddPointOfInterestCommand;
import com.thesss.platform.land.application.dto.command.DefineLandBoundaryCommand;
import com.thesss.platform.land.domain.model.LandRecordId;

public interface LinkGeospatialDataUseCase { // REQ-2-020, REQ-1.3-003

    /**
     * Defines or updates the geospatial boundary for a farm land record.
     *
     * @param landRecordId The ID of the farm land record.
     * @param command      The command object containing the boundary geometry.
     * @throws com.thesss.platform.land.domain.exception.FarmLandRecordNotFoundException If the land record is not found.
     * @throws com.thesss.platform.land.domain.exception.InvalidGeometryException If the provided geometry is invalid.
     */
    void defineLandBoundary(LandRecordId landRecordId, DefineLandBoundaryCommand command);

    /**
     * Adds a point of interest to a farm land record.
     *
     * @param landRecordId The ID of the farm land record.
     * @param command      The command object containing the POI location and details.
     * @throws com.thesss.platform.land.domain.exception.FarmLandRecordNotFoundException If the land record is not found.
      * @throws com.thesss.platform.land.domain.exception.InvalidGeometryException If the provided geometry is invalid.
     */
    void addPointOfInterest(LandRecordId landRecordId, AddPointOfInterestCommand command);
}