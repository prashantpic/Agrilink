package com.thesss.platform.land.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data // REQ-2-001, REQ-2-003, REQ-2-004, REQ-2-005, REQ-2-006, REQ-2-007, REQ-2-008, REQ-2-009, REQ-2-010, REQ-2-011, REQ-2-012, REQ-2-013, REQ-2-014, REQ-2-015, REQ-2-016, REQ-2-019
@Builder
public class CreateFarmLandRecordRequest {

    @NotNull(message = "Farmer ID is required.")
    private UUID farmerId; // REQ-2-003

    @NotBlank(message = "Parcel ID is required.")
    @Size(max = 50, message = "Parcel ID must not exceed 50 characters.")
    private String parcelId; // REQ-2-004

    @Size(max = 100, message = "Land name must not exceed 100 characters.")
    private String landName; // REQ-2-005

    @NotNull(message = "Total area is required.")
    @Valid
    private AreaRequest totalArea; // REQ-2-006

    @Valid
    private AreaRequest cultivableArea; // REQ-2-006

    @NotBlank(message = "Ownership type is required.")
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

    @Valid
    private List<OwnershipDocumentRequest> ownershipDocuments; // REQ-2-008

    @Valid
    private LeaseDetailsRequest leaseDetails; // REQ-2-009 (Can be null if not leased)

    @Valid
    private List<SoilTestHistoryEntryRequest> soilTestHistory; // REQ-2-012

    // Nested DTO for Area
    @Data
    @Builder
    public static class AreaRequest {
        @NotNull(message = "Area value is required.")
        @DecimalMin(value = "0.0", inclusive = false, message = "Area value must be positive.")
        private Double value;

        @NotBlank(message = "Area unit is required.")
        @Size(max = 20, message = "Area unit code must not exceed 20 characters.")
        private String unit; // Code from Master Data
    }

    // Nested DTO for Elevation
    @Data
    @Builder
    public static class ElevationRequest {
        @NotNull(message = "Elevation value is required.")
        private Double value;

        @NotBlank(message = "Elevation unit is required.")
        @Size(max = 20, message = "Elevation unit code must not exceed 20 characters.")
        private String unit; // Code from Master Data
    }
}