package com.thesss.platform.land.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data // REQ-2-001 to REQ-2-021
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null fields from JSON response
public class FarmLandRecordResponse {

    private UUID id; // REQ-2-002
    private UUID farmerId; // REQ-2-003
    private String parcelId; // REQ-2-004
    private String landName; // REQ-2-005

    private AreaResponse totalArea; // REQ-2-006
    private AreaResponse cultivableArea; // REQ-2-006
    private AreaResponse calculatedArea; // Calculated from geometry, REQ-2-006, REQ-1.3-004
    private Boolean hasAreaDiscrepancy; // REQ-1.3-004

    private String ownershipType; // REQ-2-007
    private String landUseCategory; // REQ-2-010
    private String soilType; // REQ-2-011
    private String irrigationMethod; // REQ-2-013
    private String topography; // REQ-2-014
    private String accessMethod; // REQ-2-015
    private ElevationResponse elevation; // REQ-2-016

    private List<OwnershipDocumentResponse> ownershipDocuments; // REQ-2-008
    private LeaseDetailsResponse leaseDetails; // REQ-2-009
    private List<SoilTestHistoryEntryResponse> soilTestHistory; // REQ-2-012
    private List<ApprovalHistoryEntryResponse> approvalHistory; // REQ-2-021

    private String status; // REQ-2-019 (e.g., "ACTIVE", "PENDING_APPROVAL")

    // Geospatial Data as GeoJSON strings
    private String boundaryGeoJson; // REQ-2-020
    private List<PointOfInterestResponse> pointsOfInterest; // REQ-2-020

    private AuditInfoResponse auditInfo; // REQ-2-018

    // Placeholder for linked crop history summary
    // private CropHistorySummaryResponse cropHistorySummary; // REQ-2-017

    // Nested DTO for Area
    @Data
    @Builder
    public static class AreaResponse {
        private Double value;
        private String unit;
    }

    // Nested DTO for Elevation
    @Data
    @Builder
    public static class ElevationResponse {
        private Double value;
        private String unit;
    }

    // Nested DTO for AuditInfo
    @Data
    @Builder
    public static class AuditInfoResponse {
        private String createdBy;
        private Instant createdDate;
        private String lastModifiedBy;
        private Instant lastModifiedDate;
    }

    // Placeholder for CropHistorySummary DTO
    // @Data
    // @Builder
    // public static class CropHistorySummaryResponse {
    //     private int numberOfCycles;
    //     private String mostRecentCrop;
    //     private double averageYieldPerHectare;
    // }
}