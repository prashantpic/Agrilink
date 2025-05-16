package com.thesss.platform.land.infrastructure.persistence.mapper;

import com.thesss.platform.land.domain.model.*;
import com.thesss.platform.land.infrastructure.persistence.entity.*;
import org.mapstruct.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE)
public abstract class FarmLandRecordPersistenceMapper {

    @Mapping(target = "id", expression = "java(domain.getId() != null ? domain.getId().getValue() : null)")
    @Mapping(target = "farmerId", expression = "java(domain.getFarmerId() != null ? domain.getFarmerId().getValue() : null)")
    @Mapping(target = "totalAreaValue", source = "totalArea.value")
    @Mapping(target = "totalAreaUnit", source = "totalArea.unit")
    @Mapping(target = "cultivableAreaValue", source = "cultivableArea.value")
    @Mapping(target = "cultivableAreaUnit", source = "cultivableArea.unit")
    @Mapping(target = "elevationValue", source = "elevation.value")
    @Mapping(target = "elevationUnit", source = "elevation.unit")
    @Mapping(target = "leaseDetails", source = "leaseDetails", qualifiedByName = "mapLeaseDetailsToJpa")
    @Mapping(target = "ownershipDocuments", source = "ownershipDocuments", qualifiedByName = "mapOwnershipDocumentsToJpa")
    @Mapping(target = "soilTestHistory", source = "soilTestHistory", qualifiedByName = "mapSoilTestHistoryToJpa")
    @Mapping(target = "approvalHistory", source = "approvalHistory", qualifiedByName = "mapApprovalHistoryToJpa")
    @Mapping(target = "boundary", source = "geospatialData.boundary")
    @Mapping(target = "pointsOfInterest", source = "geospatialData.pointsOfInterest", qualifiedByName = "mapPointsOfInterestDataToJpaEntities")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "parcelRegionCode", ignore = true) // This should be set explicitly in service or from config
    @Mapping(target = "isNew", ignore = true)
    public abstract FarmLandRecordJpaEntity toJpaEntity(FarmLandRecord domain);


    @Mapping(target = "id", expression = "java(domain.getId() != null ? domain.getId().getValue() : null)")
    @Mapping(target = "farmerId", expression = "java(domain.getFarmerId() != null ? domain.getFarmerId().getValue() : null)")
    @Mapping(target = "totalAreaValue", source = "totalArea.value")
    @Mapping(target = "totalAreaUnit", source = "totalArea.unit")
    @Mapping(target = "cultivableAreaValue", source = "cultivableArea.value")
    @Mapping(target = "cultivableAreaUnit", source = "cultivableArea.unit")
    @Mapping(target = "elevationValue", source = "elevation.value")
    @Mapping(target = "elevationUnit", source = "elevation.unit")
    @Mapping(target = "leaseDetails", source = "leaseDetails", qualifiedByName = "mapLeaseDetailsToJpa")
    @Mapping(target = "ownershipDocuments", source = "ownershipDocuments", qualifiedByName = "mapOwnershipDocumentsToJpa")
    @Mapping(target = "soilTestHistory", source = "soilTestHistory", qualifiedByName = "mapSoilTestHistoryToJpa")
    @Mapping(target = "approvalHistory", source = "approvalHistory", qualifiedByName = "mapApprovalHistoryToJpa")
    @Mapping(target = "boundary", source = "geospatialData.boundary")
    @Mapping(target = "pointsOfInterest", source = "geospatialData.pointsOfInterest", qualifiedByName = "mapPointsOfInterestDataToJpaEntities")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "parcelRegionCode", ignore = true)
    @Mapping(target = "isNew", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract FarmLandRecordJpaEntity updateJpaEntity(FarmLandRecord domain, @MappingTarget FarmLandRecordJpaEntity jpaEntity);


    @Named("mapLeaseDetailsToJpa")
    protected LeaseDetailsJpa mapLeaseDetailsToJpa(LeaseDetails domain) {
        if (domain == null) return null;
        LeaseDetailsJpa jpa = new LeaseDetailsJpa();
        jpa.setId(domain.getId() != null ? domain.getId() : UUID.randomUUID());
        jpa.setLessorName(domain.getLessorName());
        jpa.setLessorContact(domain.getLessorContact());
        jpa.setLeaseStartDate(domain.getLeaseStartDate());
        jpa.setLeaseEndDate(domain.getLeaseEndDate());
        jpa.setLeaseTerms(domain.getLeaseTerms());
        return jpa;
    }

    @Named("mapOwnershipDocumentsToJpa")
    protected List<OwnershipDocumentJpa> mapOwnershipDocumentsToJpa(List<OwnershipDocument> domains) {
        if (domains == null) return Collections.emptyList();
        return domains.stream().map(this::mapOwnershipDocumentToJpa).collect(Collectors.toList());
    }

    protected OwnershipDocumentJpa mapOwnershipDocumentToJpa(OwnershipDocument domain) {
        if (domain == null) return null;
        OwnershipDocumentJpa jpa = new OwnershipDocumentJpa();
        jpa.setId(domain.getId() != null ? domain.getId() : UUID.randomUUID());
        jpa.setDocumentUrl(domain.getDocumentUrl());
        jpa.setExpiryDate(domain.getExpiryDate());
        return jpa;
    }

    @Named("mapSoilTestHistoryToJpa")
    protected List<SoilTestHistoryEntryJpa> mapSoilTestHistoryToJpa(List<SoilTestHistoryEntry> domains) {
        if (domains == null) return Collections.emptyList();
        return domains.stream().map(this::mapSoilTestHistoryEntryToJpa).collect(Collectors.toList());
    }

    protected SoilTestHistoryEntryJpa mapSoilTestHistoryEntryToJpa(SoilTestHistoryEntry domain) {
        if (domain == null) return null;
        SoilTestHistoryEntryJpa jpa = new SoilTestHistoryEntryJpa();
        jpa.setId(domain.getId() != null ? domain.getId() : UUID.randomUUID());
        jpa.setTestDate(domain.getTestDate());
        jpa.setPH(domain.getPH());
        if (domain.getNitrogen() != null) {
            jpa.setNitrogenValue(domain.getNitrogen().getValue());
            jpa.setNitrogenUnit(domain.getNitrogen().getUnit());
        }
        if (domain.getPhosphorus() != null) {
            jpa.setPhosphorusValue(domain.getPhosphorus().getValue());
            jpa.setPhosphorusUnit(domain.getPhosphorus().getUnit());
        }
        if (domain.getPotassium() != null) {
            jpa.setPotassiumValue(domain.getPotassium().getValue());
            jpa.setPotassiumUnit(domain.getPotassium().getUnit());
        }
        jpa.setMicronutrients(domain.getMicronutrients());
        jpa.setTestReportUrl(domain.getTestReportUrl());
        return jpa;
    }

    @Named("mapApprovalHistoryToJpa")
    protected List<ApprovalHistoryEntryJpa> mapApprovalHistoryToJpa(List<ApprovalHistoryEntry> domains) {
        if (domains == null) return Collections.emptyList();
        return domains.stream().map(this::mapApprovalHistoryEntryToJpa).collect(Collectors.toList());
    }

    protected ApprovalHistoryEntryJpa mapApprovalHistoryEntryToJpa(ApprovalHistoryEntry domain) {
        if (domain == null) return null;
        ApprovalHistoryEntryJpa jpa = new ApprovalHistoryEntryJpa();
        jpa.setId(domain.getId() != null ? domain.getId() : UUID.randomUUID());
        jpa.setFieldChanged(domain.getFieldChanged());
        jpa.setPreviousValue(domain.getPreviousValue());
        jpa.setNewValue(domain.getNewValue());
        jpa.setStatus(domain.getStatus());
        jpa.setSubmittedBy(domain.getSubmittedBy());
        jpa.setSubmissionDate(domain.getSubmissionDate());
        jpa.setApprovedBy(domain.getApprovedBy());
        jpa.setApprovalDate(domain.getApprovalDate());
        jpa.setComments(domain.getComments());
        return jpa;
    }

    @Named("mapPointsOfInterestDataToJpaEntities")
    protected List<PointOfInterestJpaEntity> mapPointsOfInterestDataToJpaEntities(List<PointOfInterestData> domains) {
        if (domains == null) return Collections.emptyList();
        return domains.stream().map(this::mapPointOfInterestDataToJpaEntity).collect(Collectors.toList());
    }

    protected PointOfInterestJpaEntity mapPointOfInterestDataToJpaEntity(PointOfInterestData domain) {
        if (domain == null) return null;
        PointOfInterestJpaEntity jpa = new PointOfInterestJpaEntity();
        jpa.setId(UUID.randomUUID()); // POIs are value objects in domain, so new ID for JPA entity
        jpa.setLocation(domain.getLocation());
        jpa.setName(domain.getName());
        jpa.setDescription(domain.getDescription());
        return jpa;
    }


    @Mapping(target = "id", expression = "java(jpaEntity.getId() != null ? new LandRecordId(jpaEntity.getId()) : null)")
    @Mapping(target = "farmerId", expression = "java(jpaEntity.getFarmerId() != null ? new FarmerId(jpaEntity.getFarmerId()) : null)")
    @Mapping(target = "totalArea", source = ".", qualifiedByName = "jpaFieldsToTotalAreaDomain")
    @Mapping(target = "cultivableArea", source = ".", qualifiedByName = "jpaFieldsToCultivableAreaDomain")
    @Mapping(target = "elevation", source = ".", qualifiedByName = "jpaFieldsToElevationDomain")
    @Mapping(target = "leaseDetails", source = "leaseDetails", qualifiedByName = "mapLeaseDetailsJpaToDomain")
    @Mapping(target = "ownershipDocuments", source = "ownershipDocuments", qualifiedByName = "mapOwnershipDocumentsJpaToDomain")
    @Mapping(target = "soilTestHistory", source = "soilTestHistory", qualifiedByName = "mapSoilTestHistoryJpaToDomain")
    @Mapping(target = "approvalHistory", source = "approvalHistory", qualifiedByName = "mapApprovalHistoryJpaToDomain")
    @Mapping(target = "geospatialData", source = ".", qualifiedByName = "jpaFieldsToGeospatialDataDomain")
    @Mapping(target = "auditInfo", source = ".", qualifiedByName = "jpaFieldsToAuditInfoDomain")
    public abstract FarmLandRecord toDomain(FarmLandRecordJpaEntity jpaEntity);

    @Named("mapLeaseDetailsJpaToDomain")
    protected LeaseDetails mapLeaseDetailsJpaToDomain(LeaseDetailsJpa jpa) {
        if (jpa == null) return null;
        return new LeaseDetails(jpa.getId(), jpa.getLessorName(), jpa.getLessorContact(), jpa.getLeaseStartDate(), jpa.getLeaseEndDate(), jpa.getLeaseTerms());
    }

    @Named("mapOwnershipDocumentsJpaToDomain")
    protected List<OwnershipDocument> mapOwnershipDocumentsJpaToDomain(List<OwnershipDocumentJpa> jpas) {
        if (jpas == null) return Collections.emptyList();
        return jpas.stream().map(this::mapOwnershipDocumentJpaToDomain).collect(Collectors.toList());
    }

    protected OwnershipDocument mapOwnershipDocumentJpaToDomain(OwnershipDocumentJpa jpa) {
        if (jpa == null) return null;
        return new OwnershipDocument(jpa.getId(), jpa.getDocumentUrl(), jpa.getExpiryDate());
    }

    @Named("mapSoilTestHistoryJpaToDomain")
    protected List<SoilTestHistoryEntry> mapSoilTestHistoryJpaToDomain(List<SoilTestHistoryEntryJpa> jpas) {
        if (jpas == null) return Collections.emptyList();
        return jpas.stream().map(this::mapSoilTestHistoryEntryJpaToDomain).collect(Collectors.toList());
    }

    protected SoilTestHistoryEntry mapSoilTestHistoryEntryJpaToDomain(SoilTestHistoryEntryJpa jpa) {
        if (jpa == null) return null;
        NutrientLevel nitrogen = (jpa.getNitrogenValue() != null && jpa.getNitrogenUnit() != null) ? new NutrientLevel(jpa.getNitrogenValue(), jpa.getNitrogenUnit()) : null;
        NutrientLevel phosphorus = (jpa.getPhosphorusValue() != null && jpa.getPhosphorusUnit() != null) ? new NutrientLevel(jpa.getPhosphorusValue(), jpa.getPhosphorusUnit()) : null;
        NutrientLevel potassium = (jpa.getPotassiumValue() != null && jpa.getPotassiumUnit() != null) ? new NutrientLevel(jpa.getPotassiumValue(), jpa.getPotassiumUnit()) : null;
        return new SoilTestHistoryEntry(jpa.getId(), jpa.getTestDate(), jpa.getPH(), nitrogen, phosphorus, potassium, jpa.getMicronutrients(), jpa.getTestReportUrl());
    }

    @Named("mapApprovalHistoryJpaToDomain")
    protected List<ApprovalHistoryEntry> mapApprovalHistoryJpaToDomain(List<ApprovalHistoryEntryJpa> jpas) {
        if (jpas == null) return Collections.emptyList();
        return jpas.stream().map(this::mapApprovalHistoryEntryJpaToDomain).collect(Collectors.toList());
    }

    protected ApprovalHistoryEntry mapApprovalHistoryEntryJpaToDomain(ApprovalHistoryEntryJpa jpa) {
        if (jpa == null) return null;
        return new ApprovalHistoryEntry(jpa.getId(), jpa.getFieldChanged(), jpa.getPreviousValue(), jpa.getNewValue(), jpa.getStatus(), jpa.getSubmittedBy(), jpa.getSubmissionDate(), jpa.getApprovedBy(), jpa.getApprovalDate(), jpa.getComments());
    }

    @Named("jpaFieldsToGeospatialDataDomain")
    protected GeospatialData jpaFieldsToGeospatialDataDomain(FarmLandRecordJpaEntity jpaEntity) {
        List<PointOfInterestData> pois = Collections.emptyList();
        if (jpaEntity.getPointsOfInterest() != null) {
            pois = jpaEntity.getPointsOfInterest().stream()
                .map(this::mapPointOfInterestJpaToDomain)
                .collect(Collectors.toList());
        }
        if (jpaEntity.getBoundary() == null && pois.isEmpty()) {
            return null;
        }
        return new GeospatialData(jpaEntity.getBoundary(), pois);
    }

    protected PointOfInterestData mapPointOfInterestJpaToDomain(PointOfInterestJpaEntity jpa) {
        if (jpa == null) return null;
        return new PointOfInterestData(jpa.getLocation(), jpa.getName(), jpa.getDescription());
    }

    @Named("jpaFieldsToAuditInfoDomain")
    protected AuditInfo jpaFieldsToAuditInfoDomain(FarmLandRecordJpaEntity jpaEntity) {
        return new AuditInfo(jpaEntity.getCreatedBy(), jpaEntity.getCreatedDate(), jpaEntity.getLastModifiedBy(), jpaEntity.getLastModifiedDate());
    }

    @Named("jpaFieldsToTotalAreaDomain")
    protected Area jpaFieldsToTotalAreaDomain(FarmLandRecordJpaEntity jpaEntity) {
        if (jpaEntity.getTotalAreaValue() == null || jpaEntity.getTotalAreaUnit() == null) return null;
        return new Area(jpaEntity.getTotalAreaValue(), jpaEntity.getTotalAreaUnit());
    }

    @Named("jpaFieldsToCultivableAreaDomain")
    protected Area jpaFieldsToCultivableAreaDomain(FarmLandRecordJpaEntity jpaEntity) {
        if (jpaEntity.getCultivableAreaValue() == null || jpaEntity.getCultivableAreaUnit() == null) return null;
        return new Area(jpaEntity.getCultivableAreaValue(), jpaEntity.getCultivableAreaUnit());
    }

    @Named("jpaFieldsToElevationDomain")
    protected Elevation jpaFieldsToElevationDomain(FarmLandRecordJpaEntity jpaEntity) {
        if (jpaEntity.getElevationValue() == null || jpaEntity.getElevationUnit() == null) return null;
        return new Elevation(jpaEntity.getElevationValue(), jpaEntity.getElevationUnit());
    }
}