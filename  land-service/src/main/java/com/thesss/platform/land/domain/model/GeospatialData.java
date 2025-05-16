package com.thesss.platform.land.domain.model;

import lombok.Value;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.MultiPolygon;
import com.thesss.platform.land.domain.exception.InvalidGeometryException;


import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Value Object representing the geospatial data linked to a farm land record.
 * REQ-2-020, REQ-1.3-003
 */
@Value // Lombok annotation for immutable class
public class GeospatialData {
    Geometry boundary; // JTS Geometry (Polygon or MultiPolygon), can be null if not defined
    List<PointOfInterestData> pointsOfInterest;

    public GeospatialData(Geometry boundary, List<PointOfInterestData> pointsOfInterest) {
        if (boundary != null) {
            if (!(boundary instanceof Polygon || boundary instanceof MultiPolygon)) {
                throw new InvalidGeometryException("Boundary geometry must be a Polygon or MultiPolygon.");
            }
            if (!boundary.isValid()) {
                throw new InvalidGeometryException("Boundary geometry is not valid (e.g., self-intersecting).");
            }
        }
        this.boundary = boundary;
        this.pointsOfInterest = pointsOfInterest != null ? List.copyOf(pointsOfInterest) : Collections.emptyList();
    }
}