package com.thesss.platform.land.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data // REQ-2-020, REQ-1.3-003
@Builder
public class DefineBoundaryRequest {

    @NotBlank(message = "Boundary GeoJSON is required.")
    // Add custom validation for GeoJSON format if possible (e.g., @GeoJsonFormat(type = {Polygon.class, MultiPolygon.class}))
    // For now, rely on service layer to parse and validate
    private String boundaryGeoJson; // GeoJSON string for Polygon or MultiPolygon
}