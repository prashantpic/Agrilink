package com.thesss.platform.land.application.dto.command;

import lombok.Builder;
import lombok.Value;
import org.locationtech.jts.geom.Point;

// Command object for adding a Point of Interest
@Value // Immutable
@Builder // Builder pattern for easy construction
public class AddPointOfInterestCommand { // REQ-2-020, REQ-1.3-003
    Point locationGeometry; // JTS Point Geometry
    String name;
    String description; // Optional
}