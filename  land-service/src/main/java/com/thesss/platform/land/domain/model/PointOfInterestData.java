package com.thesss.platform.land.domain.model;

import lombok.Value;
import org.locationtech.jts.geom.Point;
import com.thesss.platform.land.domain.exception.InvalidGeometryException;
import java.util.Objects;

/**
 * Value Object representing a single Point of Interest.
 * REQ-2-020, REQ-1.3-003
 */
@Value // Lombok annotation for immutable class
public class PointOfInterestData {
    Point location; // JTS Point Geometry
    String name;
    String description; // Optional

    public PointOfInterestData(Point location, String name, String description) {
        if (location == null) {
            throw new IllegalArgumentException("POI location (Point geometry) cannot be null.");
        }
        if (!location.isValid()) {
            throw new InvalidGeometryException("POI location geometry is not valid.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("POI name cannot be null or blank.");
        }

        this.location = location;
        this.name = name;
        this.description = description;
    }
}