package com.thesss.platform.land.application.mapper;

import com.thesss.platform.land.application.dto.command.*;
import com.thesss.platform.land.application.dto.query.*;
import com.thesss.platform.land.domain.model.*;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring") // REQ-2-001
public abstract class FarmLandRecordAppMapper {

    // --- Command to Domain Mapping ---
    @Mapping(target = "id", expression = "java(com.thesss.platform.land.domain.model.LandRecordId.random())") // Generate ID for new domain object
    @Mapping(target = "farmerId", source = "farmerId", qualifiedByName = "uuidToFarmerId")
    @Mapping(target = "totalArea", source = "totalArea") // Assuming direct mapping for Area VO
    @Mapping(target = "cultivableArea", source = "cultivableArea") // Assuming direct mapping for Area VO
    @Mapping(target = "elevation", source = "elevation") // Assuming direct mapping for Elevation VO
    @Mapping(target = "ownershipDocuments", source = "ownershipDocuments", qualifiedByName = "mapOwnershipDocumentCreateCommands")
    @Mapping(target = "leaseDetails", source = "leaseDetails", qualifiedByName = "mapLeaseDetailsCreateCommand")
    @Mapping(target = "soilTestHistory", source = "soilTestHistory", qualifiedByName = "mapSoilTestHistoryCreateCommands")
    @Mapping(target = "status", ignore = true) // Status managed by service/domain logic
    @Mapping(target = "geospatialData", ignore = true) // Linked via separate use case
    @Mapping(target = "approvalHistory", ignore = true) // Managed internally
    @Mapping(target = "auditInfo", ignore = true) // Handled by JPA Auditing
    public abstract FarmLandRecord toDomain(CreateFarmLandRecordCommand command);

    @Named("mapOwnershipDocumentCreateCommands")
    protected List<OwnershipDocument> mapOwnershipDocumentCreateCommands(List<CreateFarmLandRecordCommand.OwnershipDocumentCommand> commands) {
        if (commands == null) return List.of();
        return commands.stream().map(this::mapOwnershipDocumentCreateCommand).collect(Collectors.toList());
    }
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    protected abstract OwnershipDocument mapOwnershipDocumentCreateCommand(CreateFarmLandRecordCommand.OwnershipDocumentCommand command);

    @Named("mapLeaseDetailsCreateCommand")
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    protected abstract LeaseDetails mapLeaseDetailsCreateCommand(CreateFarmLandRecordCommand.LeaseDetailsCommand command);

    @Named("mapSoilTestHistoryCreateCommands")
    protected List<SoilTestHistoryEntry> mapSoilTestHistoryCreateCommands(List<CreateFarmLandRecordCommand.SoilTestHistoryEntryCommand> commands) {
        if (commands == null) return List.of();
        return commands.stream().map(this::mapSoilTestHistoryCreateCommand).collect(Collectors.toList());
    }
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "nitrogen", source = "nitrogen")
    @Mapping(target = "phosphorus", source = "phosphorus")
    @Mapping(target = "potassium", source = "potassium")
    protected abstract SoilTestHistoryEntry mapSoilTestHistoryCreateCommand(CreateFarmLandRecordCommand.SoilTestHistoryEntryCommand command);
    protected abstract com.thesss.platform.land.domain.model.NutrientLevel mapNutrientLevelCommandToDomain(CreateFarmLandRecordCommand.NutrientLevelCommand command);


    // --- Update Command to Domain Mapping ---
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "farmerId", ignore = true)
    @Mapping(target = "totalArea", source = "totalArea")
    @Mapping(target = "cultivableArea", source = "cultivableArea")
    @Mapping(target = "elevation", source = "elevation")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "geospatialData", ignore = true)
    @Mapping(target = "approvalHistory", ignore = true)
    @Mapping(target = "auditInfo", ignore = true)
    // Collections require custom logic or specific MapStruct strategies for updates
    public abstract void updateDomainFromCommand(UpdateFarmLandRecordCommand command, @MappingTarget FarmLandRecord farmLandRecord);


    // --- Domain to Query Mapping ---
    @Mapping(target = "id", source = "id", qualifiedByName = "landRecordIdToUuid")
    @Mapping(target = "farmerId", source = "farmerId", qualifiedByName = "farmerIdToUuid")
    @Mapping(target = "totalArea", source = "totalArea", qualifiedByName = "areaDomainToQueryVO")
    @Mapping(target = "cultivableArea", source = "cultivableArea", qualifiedByName = "areaDomainToQueryVO")
    @Mapping(target = "elevation", source = "elevation", qualifiedByName = "elevationDomainToQueryVO")
    @Mapping(target = "leaseDetails", source = "leaseDetails", qualifiedByName = "leaseDetailsDomainToQuery")
    @Mapping(target = "ownershipDocuments", source = "ownershipDocuments", qualifiedByName = "ownershipDocumentsDomainToQuery")
    @Mapping(target = "soilTestHistory", source = "soilTestHistory", qualifiedByName = "soilTestHistoryDomainToQuery")
    @Mapping(target = "approvalHistory", source = "approvalHistory", qualifiedByName = "approvalHistoryDomainToQuery")
    @Mapping(target = "geospatialData", source = "geospatialData", qualifiedByName = "geospatialDataDomainToQuery")
    @Mapping(target = "auditInfo", source = "auditInfo", qualifiedByName = "auditInfoDomainToQueryVO")
    @Mapping(target = "calculatedArea", ignore = true) // Set by service
    @Mapping(target = "hasAreaDiscrepancy", ignore = true) // Set by service
    @Mapping(target = "cropHistorySummary", ignore = true) // Set by service
    public abstract FarmLandRecordQuery toQueryResult(FarmLandRecord farmLandRecord);

    @Named("leaseDetailsDomainToQuery")
    protected abstract LeaseDetailsQuery leaseDetailsDomainToQuery(LeaseDetails domain);

    @Named("ownershipDocumentsDomainToQuery")
    protected abstract List<OwnershipDocumentQuery> ownershipDocumentsDomainToQuery(List<OwnershipDocument> domains);
    protected abstract OwnershipDocumentQuery ownershipDocumentDomainToQuery(OwnershipDocument domain);

    @Named("soilTestHistoryDomainToQuery")
    protected abstract List<SoilTestHistoryEntryQuery> soilTestHistoryDomainToQuery(List<SoilTestHistoryEntry> domains);

    @Mapping(target = "nitrogen", source = "nitrogen", qualifiedByName = "nutrientLevelDomainToQueryVO")
    @Mapping(target = "phosphorus", source = "phosphorus", qualifiedByName = "nutrientLevelDomainToQueryVO")
    @Mapping(target = "potassium", source = "potassium", qualifiedByName = "nutrientLevelDomainToQueryVO")
    protected abstract SoilTestHistoryEntryQuery soilTestHistoryEntryDomainToQuery(SoilTestHistoryEntry domain);

    @Named("approvalHistoryDomainToQuery")
    protected abstract List<ApprovalHistoryEntryQuery> approvalHistoryDomainToQuery(List<ApprovalHistoryEntry> domains); // REQ-2-021
    protected abstract ApprovalHistoryEntryQuery approvalHistoryEntryDomainToQuery(ApprovalHistoryEntry domain); // REQ-2-021

    @Named("geospatialDataDomainToQuery")
    @Mapping(target = "boundaryGeometry", source = "boundary")
    @Mapping(target = "pointsOfInterest", source = "pointsOfInterest", qualifiedByName = "pointOfInterestDataListToQueryList")
    protected abstract com.thesss.platform.land.application.dto.query.GeospatialDataQuery geospatialDataDomainToQuery(GeospatialData domain); // REQ-2-020

    @Named("pointOfInterestDataListToQueryList")
    protected List<PointOfInterestQuery> pointOfInterestDataListToQueryList(List<PointOfInterestData> list) {
        if (list == null) return List.of();
        return list.stream().map(this::pointOfInterestDataToQuery).collect(Collectors.toList());
    }
    @Mapping(target = "locationGeometry", source = "location")
    protected abstract PointOfInterestQuery pointOfInterestDataToQuery(PointOfInterestData domain); // REQ-2-020


    // --- Value Object Mappings ---
    @Named("uuidToLandRecordId")
    protected LandRecordId uuidToLandRecordId(UUID uuid) {
        return uuid != null ? new LandRecordId(uuid) : null;
    }
    @Named("landRecordIdToUuid")
    protected UUID landRecordIdToUuid(LandRecordId id) {
        return id != null ? id.getValue() : null;
    }

    @Named("uuidToFarmerId")
    protected FarmerId uuidToFarmerId(UUID uuid) {
        return uuid != null ? new FarmerId(uuid) : null;
    }
    @Named("farmerIdToUuid")
    protected UUID farmerIdToUuid(FarmerId id) {
        return id != null ? id.getValue() : null;
    }

    @Named("areaDomainToQueryVO")
    public abstract AreaQuery areaDomainToQueryVO(Area area);
    @Named("elevationDomainToQueryVO")
    public abstract ElevationQuery elevationDomainToQueryVO(Elevation elevation);
    @Named("nutrientLevelDomainToQueryVO")
    public abstract NutrientLevelQuery nutrientLevelDomainToQueryVO(com.thesss.platform.land.domain.model.NutrientLevel nutrientLevel);
    @Named("auditInfoDomainToQueryVO")
    public abstract AuditInfoQuery auditInfoDomainToQueryVO(AuditInfo auditInfo);

    // Mapping for Command VOs to Domain VOs (if structures are identical, MapStruct handles it)
    public abstract Area toAreaDomain(CreateFarmLandRecordCommand.AreaCommand command);
    public abstract Elevation toElevationDomain(CreateFarmLandRecordCommand.ElevationCommand command);

    public abstract Area toAreaDomain(UpdateFarmLandRecordCommand.AreaCommand command);
    public abstract Elevation toElevationDomain(UpdateFarmLandRecordCommand.ElevationCommand command);

    public abstract com.thesss.platform.land.domain.model.NutrientLevel toNutrientLevelDomain(CreateFarmLandRecordCommand.NutrientLevelCommand command);
    public abstract com.thesss.platform.land.domain.model.NutrientLevel toNutrientLevelDomain(UpdateFarmLandRecordCommand.NutrientLevelCommand command);
}