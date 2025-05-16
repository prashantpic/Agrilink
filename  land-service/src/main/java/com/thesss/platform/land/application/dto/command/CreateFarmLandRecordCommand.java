package com.thesss.platform.land.application.dto.command;

import com.thesss.platform.land.domain.model.Area;
import com.thesss.platform.land.domain.model.Elevation;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Command object for creating a FarmLandRecord
@Value // Immutable
@Builder // Builder pattern for easy construction
public class CreateFarmLandRecordCommand { // REQ-2-001
    UUID farmerId; // REQ-2-003
    String parcelId; // REQ-2-004
    String landName; // REQ-2-005
    Area totalArea; // REQ-2-006
    Area cultivableArea; // REQ-2-006
    String ownershipType; // REQ-2-007 (Code from Master Data)
    String landUseCategory; // REQ-2-010 (Code from Master Data)
    String soilType; // REQ-2-011 (Code from Master Data)
    String irrigationMethod; // REQ-2-013 (Code from Master Data)
    String topography; // REQ-2-014 (Code from Master Data)
    String accessMethod; // REQ-2-015 (Code from Master Data)
    Elevation elevation; // REQ-2-016

    List<OwnershipDocumentCommand> ownershipDocuments; // REQ-2-008
    LeaseDetailsCommand leaseDetails; // REQ-2-009 (Can be null if not leased)
    List<SoilTestHistoryEntryCommand> soilTestHistory; // REQ-2-012

    @Value // Immutable
    @Builder
    public static class OwnershipDocumentCommand { // REQ-2-008
        String documentUrl; // URL reference to object storage
        LocalDate expiryDate; // Optional
    }

    @Value // Immutable
    @Builder
    public static class LeaseDetailsCommand { // REQ-2-009
        String lessorName;
        String lessorContact;
        LocalDate leaseStartDate;
        LocalDate leaseEndDate;
        String leaseTerms;
    }

    @Value // Immutable
    @Builder
    public static class SoilTestHistoryEntryCommand { // REQ-2-012
        LocalDate testDate;
        Double pH;
        NutrientLevelCommand nitrogen; // N
        NutrientLevelCommand phosphorus; // P
        NutrientLevelCommand potassium; // K
        String micronutrients; // Free text or codes
        String testReportUrl; // URL reference to object storage report
    }

    @Value // Immutable
    @Builder
    public static class NutrientLevelCommand { // REQ-2-012
        Double value;
        String unit; // Code from Master Data
    }

    // Helper DTO for Area in command
    @Value
    @Builder
    public static class AreaCommand { // REQ-2-006
        Double value;
        String unit;
    }

    // Helper DTO for Elevation in command
    @Value
    @Builder
    public static class ElevationCommand { // REQ-2-016
        Double value;
        String unit;
    }
}