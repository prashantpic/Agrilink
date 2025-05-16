package com.thesss.platform.land.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data // REQ-2-020, REQ-1.3-003
@Builder
public class AddPointOfInterestRequest {

    @NotBlank(message = "Location GeoJSON is required.")
    // Add custom validation for GeoJSON format if possible (e.g., @GeoJsonFormat(type = Point.class))
    private String locationGeoJson; // GeoJSON string for Point

    @NotBlank(message = "Point of Interest name is required.")
    @Size(max = 100, message = "Point of Interest name must not exceed 100 characters.")
    private String name;

    @Size(max = 255, message = "Point of Interest description must not exceed 255 characters.")
    private String description; // Optional
}