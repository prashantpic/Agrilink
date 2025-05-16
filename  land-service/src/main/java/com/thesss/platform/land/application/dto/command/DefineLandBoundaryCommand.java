package com.thesss.platform.land.application.dto.command;

import lombok.Builder;
import lombok.Value;
import org.locationtech.jts.geom.Geometry;

// Command object for defining or updating a land boundary
@Value // Immutable
@Builder // Builder pattern for easy construction
public class DefineLandBoundaryCommand { // REQ-2-020, REQ-1.3-003
    Geometry boundaryGeometry; // JTS Geometry (Polygon or MultiPolygon)
}