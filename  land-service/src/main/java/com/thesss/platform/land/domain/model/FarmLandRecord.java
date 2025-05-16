package com.thesss.platform.land.domain.model;

import com.thesss.platform.land.domain.exception.InvalidAreaException;
import com.thesss.platform.land.domain.exception.InvalidGeometryException;
import com.thesss.platform.land.domain.exception.LandRecordStateException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // Keep setters private to control state changes
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.MultiPolygon;


import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain aggregate root representing a farm land record.
 * Encapsulates all attributes and business logic.
 * REQ-2-001, REQ-2-002, REQ-2-003, REQ-2-004, REQ-2-005, REQ-2-006, REQ-2-007,
 * REQ-2-008, REQ-2-009, REQ-2-010, REQ-2-011, REQ-2-012, REQ-2-013, REQ-2-014,
 * REQ-2-015, REQ-2-016, REQ-2-018, REQ-2-019, REQ-2-020, REQ-2-021, REQ-1.3-004
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA/MapStruct
public class FarmLandRecord {

    private LandRecordId id;
    private FarmerId farmerId;
    private String parcelId;
    private String landName;

    private Area totalArea;
    private Area cultivableArea;

    private String ownershipTypeCode; // Code from MasterData
    private String landUseTypeCode; // Code from MasterData
    private String soilTypeCode; // Code from MasterData
    private String irrigationMethodCode; // Code from MasterData
    private String topographyCode; // Code from MasterData
    private Elevation elevation;

    private List<OwnershipDocument> ownershipDocuments = new ArrayList<>();
    private LeaseDetails leaseDetails; // Can be null
    private List<SoilTestHistoryEntry> soilTestHistory = new ArrayList<>();

    private LandRecordStatus status;
    private GeospatialData geospatialData; // Value Object
    private List<ApprovalHistoryEntry> approvalHistory = new ArrayList<>();
    private AuditInfo auditInfo; // Value Object, managed by JPA Auditing


    // Constructor for creating new instances
    public FarmLandRecord(FarmerId farmerId, String parcelId, String landName, Area totalArea, Area cultivableArea,
                          String ownershipTypeCode, String landUseTypeCode, String soilTypeCode,
                          String irrigationMethodCode, String topographyCode, Elevation elevation) {
        this.id = LandRecordId.random(); // Generate ID internally
        this.farmerId = Objects.requireNonNull(farmerId, "Farmer ID cannot be null.");
        this.parcelId = Objects.requireNonNull(parcelId, "Parcel ID cannot be null.");
        this.landName = landName; // Can be optional
        this.totalArea = Objects.requireNonNull(totalArea, "Total area cannot be null.");
        this.cultivableArea = Objects.requireNonNull(cultivableArea, "Cultivable area cannot be null.");
        this.ownershipTypeCode = Objects.requireNonNull(ownershipTypeCode, "Ownership type code cannot be null.");
        this.landUseTypeCode = landUseTypeCode;
        this.soilTypeCode = soilTypeCode;
        this.irrigationMethodCode = irrigationMethodCode;
        this.topographyCode = topographyCode;
        this.elevation = elevation;
        this.status = LandRecordStatus.DRAFT; // Initial status REQ-2-019
        checkAreaConsistency(); // REQ-2-006
    }

    // Business logic methods

    public void updateDetails(String landName, Area totalArea, Area cultivableArea,
                              String ownershipTypeCode, String landUseTypeCode, String soilTypeCode,
                              String irrigationMethodCode, String topographyCode, Elevation elevation) {
        // Add validation and logic for updating basic details
        this.landName = landName;
        this.totalArea = Objects.requireNonNull(totalArea, "Total area cannot be null.");
        this.cultivableArea = Objects.requireNonNull(cultivableArea, "Cultivable area cannot be null.");
        this.ownershipTypeCode = Objects.requireNonNull(ownershipTypeCode, "Ownership type code cannot be null.");
        this.landUseTypeCode = landUseTypeCode;
        this.soilTypeCode = soilTypeCode;
        this.irrigationMethodCode = irrigationMethodCode;
        this.topographyCode = topographyCode;
        this.elevation = elevation;
        checkAreaConsistency(); // REQ-2-006
        // Potentially add an approval history entry or change status if critical fields change
    }


    public void checkAreaConsistency() { // REQ-2-006
        if (this.cultivableArea.getValue() > this.totalArea.getValue()) {
            // Assuming areas are in comparable units. Unit conversion should be handled before this check if needed.
            throw new InvalidAreaException("Cultivable area (" + this.cultivableArea +
                    ") cannot be greater than total area (" + this.totalArea + ").");
        }
    }

    public void defineBoundary(Geometry boundaryGeometry) { // REQ-2-020, REQ-1.3-003
        if (boundaryGeometry == null) {
            throw new InvalidGeometryException("Boundary geometry cannot be null.");
        }
        if (!(boundaryGeometry instanceof Polygon || boundaryGeometry instanceof MultiPolygon)) {
            throw new InvalidGeometryException("Boundary geometry must be a Polygon or MultiPolygon.");
        }
        if (!boundaryGeometry.isValid()) {
            throw new InvalidGeometryException("Boundary geometry is not valid.");
        }
        List<PointOfInterestData> existingPois = this.geospatialData != null ? this.geospatialData.getPointsOfInterest() : Collections.emptyList();
        this.geospatialData = new GeospatialData(boundaryGeometry, existingPois);
        // Potentially trigger an approval workflow if area discrepancy is large (handled by App Service)
        // Add approval history entry for boundary change
        addApprovalHistory("Boundary Defined/Updated", "Boundary geometry has been set/modified.", this.status.name());
    }

    public void addPointOfInterest(Point location, String name, String description) { // REQ-2-020, REQ-1.3-003
        PointOfInterestData newPoi = new PointOfInterestData(location, name, description);
        List<PointOfInterestData> currentPois = new ArrayList<>();
        Geometry currentBoundary = null;

        if (this.geospatialData != null) {
            currentPois.addAll(this.geospatialData.getPointsOfInterest());
            currentBoundary = this.geospatialData.getBoundary();
        }
        currentPois.add(newPoi);
        this.geospatialData = new GeospatialData(currentBoundary, currentPois);
        // Adding POI might or might not need approval
        addApprovalHistory("Point of Interest Added", "POI: " + name + " added.", this.status.name());
    }

    public void addOwnershipDocument(String documentUrl, LocalDate expiryDate) { // REQ-2-008
        OwnershipDocument doc = new OwnershipDocument(UUID.randomUUID(), documentUrl, expiryDate);
        // Check for duplicates based on URL?
        this.ownershipDocuments.add(doc);
        addApprovalHistory("Ownership Document Added", "Document URL: " + documentUrl, this.status.name());
    }

    public void updateOwnershipDocument(UUID documentId, String documentUrl, LocalDate expiryDate) { // REQ-2-008
        OwnershipDocument doc = this.ownershipDocuments.stream()
            .filter(d -> d.getId().equals(documentId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Ownership document with ID " + documentId + " not found."));
        doc.updateDetails(documentUrl, expiryDate);
         addApprovalHistory("Ownership Document Updated", "Document ID: " + documentId, this.status.name());
    }

    public void removeOwnershipDocument(UUID documentId) { // REQ-2-008
        boolean removed = this.ownershipDocuments.removeIf(doc -> doc.getId().equals(documentId));
        if (!removed) {
            throw new IllegalArgumentException("Ownership document with ID " + documentId + " not found for removal.");
        }
         addApprovalHistory("Ownership Document Removed", "Document ID: " + documentId, this.status.name());
    }

    public void setLeaseDetails(String lessorName, String lesseeName, String contactInfo,
                                LocalDate startDate, LocalDate endDate, String terms) { // REQ-2-009
        if (this.leaseDetails == null) {
            this.leaseDetails = new LeaseDetails(UUID.randomUUID(), lessorName, lesseeName, contactInfo, startDate, endDate, terms);
        } else {
            this.leaseDetails.updateDetails(lessorName, lesseeName, contactInfo, startDate, endDate, terms);
        }
         addApprovalHistory("Lease Details Set/Updated", "Lessor: " + lessorName, this.status.name());
    }

    public void removeLeaseDetails() { // REQ-2-009
        if (this.leaseDetails != null) {
            this.leaseDetails = null;
            addApprovalHistory("Lease Details Removed", "Lease details were removed.", this.status.name());
        }
    }

    public void addSoilTest(LocalDate testDate, NutrientLevel pH, NutrientLevel organicCarbon,
                            NutrientLevel nitrogen, NutrientLevel phosphorus, NutrientLevel potassium,
                            String micronutrients, String testReportUrl) { // REQ-2-012
        SoilTestHistoryEntry entry = new SoilTestHistoryEntry(UUID.randomUUID(), testDate, pH, organicCarbon, nitrogen, phosphorus, potassium, micronutrients, testReportUrl);
        this.soilTestHistory.add(entry);
        addApprovalHistory("Soil Test Added", "Test Date: " + testDate, this.status.name());
    }

    public void updateSoilTest(UUID entryId, LocalDate testDate, NutrientLevel pH, NutrientLevel organicCarbon,
                               NutrientLevel nitrogen, NutrientLevel phosphorus, NutrientLevel potassium,
                               String micronutrients, String testReportUrl) { // REQ-2-012
         SoilTestHistoryEntry entry = this.soilTestHistory.stream()
            .filter(e -> e.getId().equals(entryId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Soil test entry with ID " + entryId + " not found."));
        entry.updateDetails(testDate, pH, organicCarbon, nitrogen, phosphorus, potassium, micronutrients, testReportUrl);
         addApprovalHistory("Soil Test Updated", "Entry ID: " + entryId, this.status.name());
    }


    public void updateStatus(LandRecordStatus newStatus, String reason, String changedBy) { // REQ-2-019, REQ-2-021
        if (this.status == newStatus) {
            return; // No change
        }
        // Add complex status transition validation logic here if needed.
        // For example, can only move from DRAFT to PENDING_APPROVAL.
        LandRecordStatus oldStatus = this.status;
        this.status = newStatus;
        addApprovalHistory("Status Changed",
                           "Status changed from " + oldStatus + " to " + newStatus + ". Reason: " + (reason != null ? reason : "N/A"),
                           newStatus.name(), changedBy, Instant.now(), null, null, reason
        );
        // Application service will decide if external approval workflow is needed.
    }

    private void addApprovalHistory(String fieldChanged, String comments, String currentRecordStatus) { // REQ-2-021
         // Simplified version, assumes submittedBy is system or derived from context
        // Actual approval flow (approvedBy, approvalDate) managed externally or by more complex logic.
        String submittedBy = auditInfo != null ? auditInfo.getLastModifiedBy() : "system"; // Example
        Instant submissionDate = auditInfo != null ? auditInfo.getLastModifiedDate() : Instant.now(); // Example
        addApprovalHistory(fieldChanged, comments, currentRecordStatus, submittedBy, submissionDate, null, null, comments);
    }

    private void addApprovalHistory(String fieldChanged, String comments, String newRecordStatus,
                                   String submittedBy, Instant submissionDate,
                                   String approvedBy, Instant approvalDate, String approvalComments) { // REQ-2-021
        ApprovalHistoryEntry entry = new ApprovalHistoryEntry(
                UUID.randomUUID(),
                fieldChanged,
                null, // previousValue - more complex to track generally
                newRecordStatus, // newValue - here it's the new status of the record
                "LOGGED", // status of the history entry itself, e.g. LOGGED, PENDING_EXTERNAL_APPROVAL
                submittedBy,
                submissionDate,
                approvedBy,
                approvalDate,
                (comments != null ? comments + " " : "") + (approvalComments != null ? approvalComments : "")
        );
        this.approvalHistory.add(entry);
    }

    // Setter for JPA Auditing (called by framework)
    public void setAuditInfo(AuditInfo auditInfo) {
        this.auditInfo = auditInfo;
    }

    // To ensure immutable collections are exposed if needed, but Lombok @Getter on list fields returns the list itself.
    // For true immutability on getters, defensive copies are needed:
    public List<OwnershipDocument> getOwnershipDocuments() {
        return Collections.unmodifiableList(ownershipDocuments);
    }

    public List<SoilTestHistoryEntry> getSoilTestHistory() {
        return Collections.unmodifiableList(soilTestHistory);
    }

    public List<ApprovalHistoryEntry> getApprovalHistory() {
        return Collections.unmodifiableList(approvalHistory);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FarmLandRecord that = (FarmLandRecord) o;
        return Objects.equals(id, that.id); // Aggregate equality based on ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Aggregate hashCode based on ID
    }
}