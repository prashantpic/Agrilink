package com.thesss.platform.land.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data // REQ-2-001, REQ-2-004, REQ-2-005, REQ-2-006, REQ-2-007, REQ-2-008, REQ-2-009, REQ-2-010, REQ-2-011, REQ-2-012, REQ-2-013, REQ-2-014, REQ-2-015, REQ-2-016, REQ-2-019
@Builder
public class UpdateFarmLandRecordRequest {

    // Fields are optional for partial updates
    @Size(max = 50, message = "Parcel ID must not exceed 50 characters.")
    private String parcelId; // REQ-2-004

    @Size(max = 100, message = "Land name must not exceed 100 characters.")
    private String landName; // REQ-2-005

    @Valid
    private AreaRequest totalArea; // REQ-2-006

    @Valid
    private AreaRequest cultivableArea; // REQ-2-006

    @Size(max = 50, message = "Ownership type code must not exceed 50 characters.")
    private String ownershipType; // REQ-2-007 (Code from Master Data)

    @Size(max = 50, message = "Land use category code must not exceed 50 characters.")
    private String landUseCategory; // REQ-2-010 (Code from Master Data)

    @Size(max = 50, message = "Soil type code must not exceed 50 characters.")
    private String soilType; // REQ-2-011 (Code from Master Data)

    @Size(max = 50, message = "Irrigation method code must not exceed 50 characters.")
    private String irrigationMethod; // REQ-2-013 (Code from Master Data)

    @Size(max = 50, message = "Topography code must not exceed 50 characters.")
    private String topography; // REQ-2-014 (Code from Master Data)

    @Size(max = 50, message = "Access method code must not exceed 50 characters.")
    private String accessMethod; // REQ-2-015 (Code from Master Data)

    @Valid
    private ElevationRequest elevation; // REQ-2-016

    // For nested entities, updates might involve adding, removing, or modifying existing ones.
    // The commands in the application layer will need to handle this logic.
    // For API DTOs, we can allow full replacement or partial updates based on IDs.
    @Valid
    private List<OwnershipDocumentRequest> ownershipDocuments; // REQ-2-008

    @Valid
    private LeaseDetailsRequest leaseDetails; // REQ-2-009 (Can be null to remove or update)

    @Valid
    private List<SoilTestHistoryEntryRequest> soilTestHistory; // REQ-2-012

    // Nested DTO for Area (reused from Create request for consistency)
    @Data
    @Builder
    public static class AreaRequest {
        @DecimalMin(value = "0.0", inclusive = false, message = "Area value must be positive.")
        private Double value;

        @Size(max = 20, message = "Area unit code must not exceed 20 characters.")
        private String unit; // Code from Master Data
    }

    // Nested DTO for Elevation (reused from Create request for consistency)
    @Data
    @Builder
    public static class ElevationRequest {
        private Double value;

        @Size(max = 20, message = "Elevation unit code must not exceed 20 characters.")
        private String unit; // Code from Master Data
    }
}