package com.thesss.platform.land.api.mapper;

import com.thesss.platform.land.api.dto.request.*;
import com.thesss.platform.land.api.dto.response.*;
import com.thesss.platform.land.application.dto.command.*;
import com.thesss.platform.land.application.dto.query.*;
import com.thesss.platform.land.infrastructure.geospatial.GeoJsonConverter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {GeoJsonConverter.class}) // REQ-2-001, REQ-2-020
public abstract class FarmLandRecordApiMapper {

    @Autowired
    protected GeoJsonConverter geoJsonConverter;

    // --- Request to Command Mapping ---
    @Mapping(target = "farmerId", source = "farmerId")
    @Mapping(target = "leaseDetails", source = "leaseDetails")
    @Mapping(target = "ownershipDocuments", source = "ownershipDocuments")
    @Mapping(target = "soilTestHistory", source = "soilTestHistory")
    @Mapping(target = "totalArea", source = "totalArea")
    @Mapping(target = "cultivableArea", source = "cultivableArea")
    @Mapping(target = "elevation", source = "elevation")
    public abstract CreateFarmLandRecordCommand toCreateFarmLandRecordCommand(CreateFarmLandRecordRequest request);

    protected abstract CreateFarmLandRecordCommand.AreaCommand mapAreaRequestToCommand(CreateFarmLandRecordRequest.AreaRequest request);
    protected abstract CreateFarmLandRecordCommand.ElevationCommand mapElevationRequestToCommand(CreateFarmLandRecordRequest.ElevationRequest request);
    protected abstract CreateFarmLandRecordCommand.OwnershipDocumentCommand mapOwnershipDocumentRequestToCommand(OwnershipDocumentRequest request);
    protected abstract CreateFarmLandRecordCommand.LeaseDetailsCommand mapLeaseDetailsRequestToCommand(LeaseDetailsRequest request);
    protected abstract CreateFarmLandRecordCommand.SoilTestHistoryEntryCommand mapSoilTestHistoryEntryRequestToCommand(SoilTestHistoryEntryRequest request);
    protected abstract CreateFarmLandRecordCommand.NutrientLevel mapNutrientRequestToCommand(SoilTestHistoryEntryRequest.NutrientRequest request);


    @Mapping(target = "leaseDetails", source = "leaseDetails")
    @Mapping(target = "ownershipDocuments", source = "ownershipDocuments")
    @Mapping(target = "soilTestHistory", source = "soilTestHistory")
    @Mapping(target = "totalArea", source = "totalArea")
    @Mapping(target = "cultivableArea", source = "cultivableArea")
    @Mapping(target = "elevation", source = "elevation")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract UpdateFarmLandRecordCommand toUpdateFarmLandRecordCommand(UpdateFarmLandRecordRequest request);

    protected abstract UpdateFarmLandRecordCommand.AreaCommand mapAreaRequestToUpdateCommand(UpdateFarmLandRecordRequest.AreaRequest request);
    protected abstract UpdateFarmLandRecordCommand.ElevationCommand mapElevationRequestToUpdateCommand(UpdateFarmLandRecordRequest.ElevationRequest request);
    protected abstract UpdateFarmLandRecordCommand.OwnershipDocumentCommand mapOwnershipDocumentRequestToUpdateCommand(OwnershipDocumentRequest request);
    protected abstract UpdateFarmLandRecordCommand.LeaseDetailsCommand mapLeaseDetailsRequestToUpdateCommand(LeaseDetailsRequest request);
    protected abstract UpdateFarmLandRecordCommand.SoilTestHistoryEntryCommand mapSoilTestHistoryEntryRequestToUpdateCommand(SoilTestHistoryEntryRequest request);
    protected abstract UpdateFarmLandRecordCommand.NutrientLevel mapNutrientRequestToUpdateCommand(SoilTestHistoryEntryRequest.NutrientRequest request);


    @Mapping(target = "boundaryGeometry", source = "boundaryGeoJson", qualifiedByName = "geoJsonToGeometry")
    public abstract DefineLandBoundaryCommand toDefineLandBoundaryCommand(DefineBoundaryRequest request);

    @Mapping(target = "locationGeometry", source = "locationGeoJson", qualifiedByName = "geoJsonToPoint")
    public abstract AddPointOfInterestCommand toAddPointOfInterestCommand(AddPointOfInterestRequest request);


    // --- Query to Response Mapping ---
    @Mapping(target = "id", source = "id")
    @Mapping(target = "farmerId", source = "farmerId")
    @Mapping(target = "leaseDetails", source = "leaseDetails")
    @Mapping(target = "ownershipDocuments", source = "ownershipDocuments")
    @Mapping(target = "soilTestHistory", source = "soilTestHistory")
    @Mapping(target = "approvalHistory", source = "approvalHistory")
    @Mapping(target = "boundaryGeoJson", source = "geospatialData.boundaryGeometry", qualifiedByName = "geometryToGeoJson")
    @Mapping(target = "pointsOfInterest", source = "geospatialData.pointsOfInterest", qualifiedByName = "poiQueriesToResponses")
    @Mapping(target = "totalArea", source = "totalArea")
    @Mapping(target = "cultivableArea", source = "cultivableArea")
    @Mapping(target = "calculatedArea", source = "calculatedArea")
    @Mapping(target = "elevation", source = "elevation")
    @Mapping(target = "auditInfo", source = "auditInfo")
    public abstract FarmLandRecordResponse toFarmLandRecordResponse(FarmLandRecordQuery query);

    protected abstract FarmLandRecordResponse.AreaResponse mapAreaQueryToResponse(FarmLandRecordAppMapper.AreaQuery query);
    protected abstract FarmLandRecordResponse.ElevationResponse mapElevationQueryToResponse(FarmLandRecordAppMapper.ElevationQuery query);
    protected abstract FarmLandRecordResponse.AuditInfoResponse mapAuditInfoQueryToResponse(FarmLandRecordAppMapper.AuditInfoQuery query);

    protected abstract LeaseDetailsResponse mapLeaseDetailsQueryToResponse(LeaseDetailsQuery query);
    protected abstract OwnershipDocumentResponse mapOwnershipDocumentQueryToResponse(OwnershipDocumentQuery query);
    protected abstract SoilTestHistoryEntryResponse mapSoilTestHistoryEntryQueryToResponse(SoilTestHistoryEntryQuery query);
    protected abstract SoilTestHistoryEntryResponse.NutrientResponse mapNutrientQueryToResponse(FarmLandRecordAppMapper.NutrientLevelQuery query);
    protected abstract ApprovalHistoryEntryResponse mapApprovalHistoryEntryQueryToResponse(ApprovalHistoryEntryQuery query);


    @Named("poiQueriesToResponses")
    protected List<PointOfInterestResponse> mapPoiQueriesToResponses(List<PointOfInterestQuery> queries) {
        if (queries == null) {
            return null;
        }
        return queries.stream()
                .map(this::mapPointOfInterestQueryToResponse)
                .collect(Collectors.toList());
    }

    @Mapping(target = "locationGeoJson", source = "locationGeometry", qualifiedByName = "geometryToGeoJson")
    protected abstract PointOfInterestResponse mapPointOfInterestQueryToResponse(PointOfInterestQuery query);


    // --- GeoJSON Converters ---
    @Named("geoJsonToGeometry")
    protected Geometry geoJsonToGeometry(String geoJson) {
        if (geoJson == null) {
            return null;
        }
        return geoJsonConverter.fromGeoJson(geoJson);
    }

    @Named("geoJsonToPoint")
    protected Point geoJsonToPoint(String geoJson) {
        if (geoJson == null) {
            return null;
        }
        Geometry geom = geoJsonConverter.fromGeoJson(geoJson);
        if (geom instanceof Point) {
            return (Point) geom;
        }
        // Optionally throw an error or handle incorrect geometry type
        return null;
    }


    @Named("geometryToGeoJson")
    protected String geometryToGeoJson(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        return geoJsonConverter.toGeoJson(geometry);
    }

    // --- Pagination Mapping ---
    public PagedResponse<FarmLandRecordResponse> toPagedResponse(Page<FarmLandRecordResponse> page) {
        return PagedResponse.fromSpringPage(page);
    }
}