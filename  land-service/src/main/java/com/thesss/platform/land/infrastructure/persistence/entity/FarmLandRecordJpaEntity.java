package com.thesss.platform.land.infrastructure.persistence.entity;

import com.thesss.platform.land.domain.model.LandRecordStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "farm_land_record")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class FarmLandRecordJpaEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "farmer_id", columnDefinition = "uuid", nullable = false)
    private UUID farmerId;

    @Column(name = "parcel_id", nullable = false)
    private String parcelId;

    @Column(name = "parcel_region_code", nullable = false) // Store region for uniqueness check
    private String parcelRegionCode;

    @Column(name = "land_name")
    private String landName;

    @Column(name = "total_area_value", nullable = false)
    private Double totalAreaValue;

    @Column(name = "total_area_unit", nullable = false)
    private String totalAreaUnit;

    @Column(name = "cultivable_area_value")
    private Double cultivableAreaValue;

    @Column(name = "cultivable_area_unit")
    private String cultivableAreaUnit;

    @Column(name = "ownership_type", nullable = false)
    private String ownershipType;

    @Column(name = "land_use_category")
    private String landUseCategory;

    @Column(name = "soil_type")
    private String soilType;

    @Column(name = "irrigation_method")
    private String irrigationMethod;

    @Column(name = "topography")
    private String topography;

    @Column(name = "access_method")
    private String accessMethod;

    @Column(name = "elevation_value")
    private Double elevationValue;

    @Column(name = "elevation_unit")
    private String elevationUnit;

    @Column(name = "boundary", columnDefinition = "geometry(GEOMETRY, 4326)")
    @JdbcTypeCode(SqlTypes.OTHER)
    private Geometry boundary;

    @OneToMany(mappedBy = "farmLandRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PointOfInterestJpaEntity> pointsOfInterest = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LandRecordStatus status;

    @OneToMany(mappedBy = "farmLandRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OwnershipDocumentJpa> ownershipDocuments = new ArrayList<>();

    @OneToOne(mappedBy = "farmLandRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private LeaseDetailsJpa leaseDetails;

    @OneToMany(mappedBy = "farmLandRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SoilTestHistoryEntryJpa> soilTestHistory = new ArrayList<>();

    @OneToMany(mappedBy = "farmLandRecord", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ApprovalHistoryEntryJpa> approvalHistory = new ArrayList<>();

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PrePersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    public void addOwnershipDocument(OwnershipDocumentJpa document) {
        this.ownershipDocuments.add(document);
        document.setFarmLandRecord(this);
    }

    public void removeOwnershipDocument(OwnershipDocumentJpa document) {
        this.ownershipDocuments.remove(document);
        document.setFarmLandRecord(null);
    }

    public void setLeaseDetails(LeaseDetailsJpa leaseDetails) {
        if (this.leaseDetails != null) {
            this.leaseDetails.setFarmLandRecord(null);
        }
        this.leaseDetails = leaseDetails;
        if (leaseDetails != null) {
            leaseDetails.setFarmLandRecord(this);
        }
    }

    public void addSoilTestHistoryEntry(SoilTestHistoryEntryJpa entry) {
        this.soilTestHistory.add(entry);
        entry.setFarmLandRecord(this);
    }

    public void removeSoilTestHistoryEntry(SoilTestHistoryEntryJpa entry) {
        this.soilTestHistory.remove(entry);
        entry.setFarmLandRecord(null);
    }

    public void addApprovalHistoryEntry(ApprovalHistoryEntryJpa entry) {
        this.approvalHistory.add(entry);
        entry.setFarmLandRecord(this);
    }

    public void addPointOfInterest(PointOfInterestJpaEntity poi) {
        this.pointsOfInterest.add(poi);
        poi.setFarmLandRecord(this);
    }

    public void removePointOfInterest(PointOfInterestJpaEntity poi) {
        this.pointsOfInterest.remove(poi);
        poi.setFarmLandRecord(null);
    }
}