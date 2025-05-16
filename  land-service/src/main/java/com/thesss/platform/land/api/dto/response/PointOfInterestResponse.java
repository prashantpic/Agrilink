package com.thesss.platform.land.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data // REQ-2-020
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PointOfInterestResponse {
    // This DTO represents a single POI within the list in FarmLandRecordResponse.
    private String locationGeoJson; // GeoJSON string for Point
    private String name;
    private String description;
}