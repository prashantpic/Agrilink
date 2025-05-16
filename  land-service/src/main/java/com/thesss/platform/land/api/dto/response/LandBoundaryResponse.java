package com.thesss.platform.land.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data // REQ-2-020
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LandBoundaryResponse {
    // This DTO might not be directly returned by an endpoint,
    // but its content (geoJson) is part of FarmLandRecordResponse.
    // If there's a specific endpoint to get *only* the boundary, this would be used.
    private String geoJson; // GeoJSON string for Polygon or MultiPolygon
}