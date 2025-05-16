package com.thesss.platform.land.application.service;

import com.thesss.platform.land.application.dto.command.*;
import com.thesss.platform.land.application.dto.query.FarmLandRecordQuery;
import com.thesss.platform.land.application.mapper.FarmLandRecordAppMapper;
import com.thesss.platform.land.application.port.in.*;
import com.thesss.platform.land.application.port.out.*;
import com.thesss.platform.land.config.AppProperties;
import com.thesss.platform.land.domain.event.FarmLandRecordCreatedEvent;
import com.thesss.platform.land.domain.event.FarmLandRecordUpdatedEvent;
import com.thesss.platform.land.domain.exception.FarmLandRecordNotFoundException;
import com.thesss.platform.land.domain.exception.InvalidGeometryException;
import com.thesss.platform.land.domain.exception.ParcelIdConflictException;
import com.thesss.platform.land.domain.model.*;
import com.thesss.platform.land.infrastructure.geospatial.AreaCalculationService;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service // REQ-2-001, REQ-2-004, REQ-2-006, REQ-2-008, REQ-2-009, REQ-2-012, REQ-2-017, REQ-2-019, REQ-2-020, REQ-2-021, REQ-1.3-004
@Transactional // Apply transactional boundaries
public class FarmLandRecordApplicationService implements
        CreateFarmLandRecordUseCase,
        UpdateFarmLandRecordUseCase,
        GetFarmLandRecordUseCase,
        LinkGeospatialDataUseCase,
        ManageLandRecordStatusUseCase {

    private final FarmLandRecordRepositoryPort farmLandRecordRepository;
    private final FarmLandRecordAppMapper appMapper;
    private final MasterDataServicePort masterDataService; // REQ-2-006, REQ-2-007, REQ-2-010, REQ-2-012, REQ-2-013, REQ-2-014, REQ-2-015, REQ-2-016, REQ-2-019
    private final ObjectStorageServicePort objectStorageService; // REQ-2-008, REQ-2-012
    private final ApprovalWorkflowServicePort approvalWorkflowService; // REQ-2-021, REQ-2-004
    private final CropServicePort cropService; // REQ-2-017
    private final FarmLandRecordEventPublisherPort eventPublisher; // REPO-LAND-SVC
    private final AreaCalculationService areaCalculationService; // REQ-2-006, REQ-1.3-004
    private final AppProperties appProperties;

    @Autowired // Constructor Injection
    public FarmLandRecordApplicationService(
            FarmLandRecordRepositoryPort farmLandRecordRepository,
            FarmLandRecordAppMapper appMapper,
            MasterDataServicePort masterDataService,
            ObjectStorageServicePort objectStorageService,
            ApprovalWorkflowServicePort approvalWorkflowService,
            CropServicePort cropService,
            FarmLandRecordEventPublisherPort eventPublisher,
            AreaCalculationService areaCalculationService,
            AppProperties appProperties) {
        this.farmLandRecordRepository = farmLandRecordRepository;
        this.appMapper = appMapper;
        this.masterDataService = masterDataService;
        this.objectStorageService = objectStorageService;
        this.approvalWorkflowService = approvalWorkflowService;
        this.cropService = cropService;
        this.eventPublisher = eventPublisher;
        this.areaCalculationService = areaCalculationService;
        this.appProperties = appProperties;
    }

    @Override // Implements CreateFarmLandRecordUseCase
    public LandRecordId createFarmLandRecord(CreateFarmLandRecordCommand command) {
        validateMasterDataCodesForCreate(command); // REQ-2-006, REQ-2-007, REQ-2-010, REQ-2-011, REQ-2-012, REQ-2-013, REQ-2-014, REQ-2-015, REQ-2-016

        if (farmLandRecordRepository.existsByParcelIdAndRegion(command.getParcelId(), appProperties.getParcelIdValidation().getUniquenessRegionCode())) { // REQ-2-004
            throw new ParcelIdConflictException("Parcel ID " + command.getParcelId() + " already exists in region " + appProperties.getParcelIdValidation().getUniquenessRegionCode());
        }

        FarmLandRecord farmLandRecord = appMapper.toDomain(command);
        farmLandRecord.checkAreaConsistency(); // REQ-2-006
        farmLandRecord.updateStatus(LandRecordStatus.PENDING_APPROVAL, "Initial record creation"); // Initial status REQ-2-019, REQ-2-021

        FarmLandRecord savedRecord = farmLandRecordRepository.save(farmLandRecord); // REQ-2-001

        eventPublisher.publishFarmLandRecordCreatedEvent(new FarmLandRecordCreatedEvent(savedRecord.getId(), savedRecord.getFarmerId())); // REPO-LAND-SVC
        approvalWorkflowService.initiateLandRecordApproval(savedRecord.getId(), savedRecord.getFarmerId(), "New Land Record Created"); // REQ-2-021, REQ-2-004

        return savedRecord.getId();
    }

    @Override // Implements UpdateFarmLandRecordUseCase
    public void updateFarmLandRecord(LandRecordId landRecordId, UpdateFarmLandRecordCommand command) {
        FarmLandRecord existingRecord = farmLandRecordRepository.findById(landRecordId) // REQ-2-001
                .orElseThrow(() -> new FarmLandRecordNotFoundException("Farm land record not found with ID: " + landRecordId.getValue()));

        validateMasterDataCodesForUpdate(command); // REQ-2-006, REQ-2-007, REQ-2-010, REQ-2-011, REQ-2-012, REQ-2-013, REQ-2-014, REQ-2-015, REQ-2-016

        boolean parcelIdChanged = command.getParcelId() != null && !command.getParcelId().equals(existingRecord.getParcelId());
        if (parcelIdChanged) {
             if (farmLandRecordRepository.existsByParcelIdAndRegion(command.getParcelId(), appProperties.getParcelIdValidation().getUniquenessRegionCode())) { // REQ-2-004
                 throw new ParcelIdConflictException("Parcel ID " + command.getParcelId() + " already exists in region " + appProperties.getParcelIdValidation().getUniquenessRegionCode());
             }
        }

        appMapper.updateDomainFromCommand(command, existingRecord);
        existingRecord.checkAreaConsistency(); // REQ-2-006

        if (parcelIdChanged) { // Check again after applying change to existingRecord
            approvalWorkflowService.initiateLandRecordApproval(landRecordId, existingRecord.getFarmerId(), "Parcel ID updated to " + existingRecord.getParcelId()); // REQ-2-021, REQ-2-004
            existingRecord.updateStatus(LandRecordStatus.PENDING_APPROVAL, "Parcel ID Change Requires Approval");
        }
        // Add similar logic for other critical field changes that require approval

        FarmLandRecord updatedRecord = farmLandRecordRepository.save(existingRecord); // REQ-2-001
        eventPublisher.publishFarmLandRecordUpdatedEvent(new FarmLandRecordUpdatedEvent(updatedRecord.getId(), updatedRecord.getFarmerId())); // REPO-LAND-SVC
    }

    @Override // Implements GetFarmLandRecordUseCase
    @Transactional(readOnly = true)
    public FarmLandRecordQuery getFarmLandRecordById(LandRecordId landRecordId) {
        FarmLandRecord farmLandRecord = farmLandRecordRepository.findById(landRecordId) // REQ-2-001
                .orElseThrow(() -> new FarmLandRecordNotFoundException("Farm land record not found with ID: " + landRecordId.getValue()));

        FarmLandRecordQuery queryResult = appMapper.toQueryResult(farmLandRecord);

        // Fetch Crop History summary - REQ-2-017 (Placeholder - actual implementation depends on CropServicePort)
        // Example: queryResult.setCropHistorySummary(cropService.getCropHistorySummaryForLand(landRecordId));

        if (farmLandRecord.getGeospatialData() != null && farmLandRecord.getGeospatialData().getBoundary() != null) {
             Area calculatedArea = areaCalculationService.calculateArea(farmLandRecord.getGeospatialData().getBoundary()); // REQ-2-006
             queryResult.setCalculatedArea(appMapper.areaDomainToQuery(calculatedArea));
             boolean hasDiscrepancy = areaCalculationService.checkAreaDiscrepancy(
                     farmLandRecord.getTotalArea(),
                     calculatedArea,
                     appProperties.getGeospatialSettings().getAreaDiscrepancyThresholdPercentage()); // REQ-1.3-004
             queryResult.setHasAreaDiscrepancy(hasDiscrepancy);
        }
        return queryResult;
    }

    @Override // Implements GetFarmLandRecordUseCase
    @Transactional(readOnly = true)
    public Page<FarmLandRecordQuery> getAllFarmLandRecords(Pageable pageable) {
        Page<FarmLandRecord> farmLandRecordPage = farmLandRecordRepository.findAll(pageable); // REQ-2-001
        return farmLandRecordPage.map(this::mapDomainToQueryWithCalculations);
    }

     @Override // Implements GetFarmLandRecordUseCase
     @Transactional(readOnly = true)
     public Page<FarmLandRecordQuery> getFarmLandRecordsByFarmerId(FarmerId farmerId, Pageable pageable) {
         Page<FarmLandRecord> farmLandRecordPage = farmLandRecordRepository.findByFarmerId(farmerId, pageable); // REQ-2-003
         return farmLandRecordPage.map(this::mapDomainToQueryWithCalculations);
     }

    private FarmLandRecordQuery mapDomainToQueryWithCalculations(FarmLandRecord farmLandRecord) {
        FarmLandRecordQuery queryResult = appMapper.toQueryResult(farmLandRecord);
        if (farmLandRecord.getGeospatialData() != null && farmLandRecord.getGeospatialData().getBoundary() != null) {
            Area calculatedArea = areaCalculationService.calculateArea(farmLandRecord.getGeospatialData().getBoundary());
            queryResult.setCalculatedArea(appMapper.areaDomainToQuery(calculatedArea));
             boolean hasDiscrepancy = areaCalculationService.checkAreaDiscrepancy(
                     farmLandRecord.getTotalArea(),
                     calculatedArea,
                     appProperties.getGeospatialSettings().getAreaDiscrepancyThresholdPercentage());
             queryResult.setHasAreaDiscrepancy(hasDiscrepancy);
        }
        return queryResult;
    }

    @Override // Implements LinkGeospatialDataUseCase
    public void defineLandBoundary(LandRecordId landRecordId, DefineLandBoundaryCommand command) {
        FarmLandRecord farmLandRecord = farmLandRecordRepository.findById(landRecordId) // REQ-2-020, REQ-1.3-003
                .orElseThrow(() -> new FarmLandRecordNotFoundException("Farm land record not found with ID: " + landRecordId.getValue()));

        Geometry boundary = command.getBoundaryGeometry();
        GeospatialData newGeospatialData = new GeospatialData(boundary,
                farmLandRecord.getGeospatialData() != null ? farmLandRecord.getGeospatialData().getPointsOfInterest() : List.of());

        farmLandRecord.defineBoundary(newGeospatialData); // Domain logic REQ-2-020

        // Area calculation and discrepancy check
        Area calculatedArea = areaCalculationService.calculateArea(boundary); // REQ-2-006
        boolean hasDiscrepancy = areaCalculationService.checkAreaDiscrepancy(
            farmLandRecord.getTotalArea(),
            calculatedArea,
            appProperties.getGeospatialSettings().getAreaDiscrepancyThresholdPercentage()
        ); // REQ-1.3-004

        if (hasDiscrepancy) {
            // Trigger approval for area discrepancy
            approvalWorkflowService.initiateLandRecordApproval(landRecordId, farmLandRecord.getFarmerId(), "Area discrepancy detected after boundary update."); // REQ-2-021
            farmLandRecord.updateStatus(LandRecordStatus.PENDING_APPROVAL, "Area Discrepancy Needs Approval");
        }

        farmLandRecordRepository.save(farmLandRecord);
        eventPublisher.publishFarmLandRecordUpdatedEvent(new FarmLandRecordUpdatedEvent(farmLandRecord.getId(), farmLandRecord.getFarmerId()));
    }

    @Override // Implements LinkGeospatialDataUseCase
    public void addPointOfInterest(LandRecordId landRecordId, AddPointOfInterestCommand command) {
        FarmLandRecord farmLandRecord = farmLandRecordRepository.findById(landRecordId) // REQ-2-020, REQ-1.3-003
                .orElseThrow(() -> new FarmLandRecordNotFoundException("Farm land record not found with ID: " + landRecordId.getValue()));

        PointOfInterestData poiData = new PointOfInterestData(command.getLocationGeometry(), command.getName(), command.getDescription());
        farmLandRecord.addPointOfInterest(poiData); // Domain logic REQ-2-020

        farmLandRecordRepository.save(farmLandRecord);
        eventPublisher.publishFarmLandRecordUpdatedEvent(new FarmLandRecordUpdatedEvent(farmLandRecord.getId(), farmLandRecord.getFarmerId()));
    }

    @Override // Implements ManageLandRecordStatusUseCase
    public void updateLandRecordStatus(LandRecordId landRecordId, String status, String reason) {
         FarmLandRecord farmLandRecord = farmLandRecordRepository.findById(landRecordId) // REQ-2-019, REQ-2-021
                 .orElseThrow(() -> new FarmLandRecordNotFoundException("Farm land record not found with ID: " + landRecordId.getValue()));

         LandRecordStatus newStatus;
         try {
              newStatus = LandRecordStatus.valueOf(status.toUpperCase());
         } catch (IllegalArgumentException e) {
             throw new IllegalArgumentException("Invalid land record status code: " + status); // REQ-2-019
         }

         boolean approvalRequired = farmLandRecord.updateStatus(newStatus, reason); // Domain logic REQ-2-019, REQ-2-021
         farmLandRecordRepository.save(farmLandRecord);

         if (approvalRequired) {
             approvalWorkflowService.initiateLandRecordApproval(landRecordId, farmLandRecord.getFarmerId(), "Status change to " + newStatus + ": " + reason); // REQ-2-021
         }
        eventPublisher.publishFarmLandRecordUpdatedEvent(new FarmLandRecordUpdatedEvent(farmLandRecord.getId(), farmLandRecord.getFarmerId()));
    }

    private void validateMasterDataCodesForCreate(CreateFarmLandRecordCommand command) {
        // Example: if (!masterDataService.isValidCode("OwnershipType", command.getOwnershipType())) { throw new IllegalArgumentException("Invalid Ownership Type"); }
        // Implement for all relevant codes: ownershipType, landUseCategory, soilType, irrigationMethod, topography, accessMethod, units for Area/Elevation/NutrientLevel.
        // REQ-2-006, REQ-2-007, REQ-2-010, REQ-2-011, REQ-2-012, REQ-2-013, REQ-2-014, REQ-2-015, REQ-2-016
        if (!masterDataService.isValidCode("OWNERSHIP_TYPE", command.getOwnershipType())) throw new IllegalArgumentException("Invalid Ownership Type code: " + command.getOwnershipType());
        if (command.getTotalArea() != null && !masterDataService.isValidCode("AREA_UNIT", command.getTotalArea().getUnit())) throw new IllegalArgumentException("Invalid Total Area unit: " + command.getTotalArea().getUnit());
        // ... more validations
    }

     private void validateMasterDataCodesForUpdate(UpdateFarmLandRecordCommand command) {
         if (command.getOwnershipType() != null && !masterDataService.isValidCode("OWNERSHIP_TYPE", command.getOwnershipType())) throw new IllegalArgumentException("Invalid Ownership Type code: " + command.getOwnershipType());
         if (command.getTotalArea() != null && command.getTotalArea().getUnit() != null && !masterDataService.isValidCode("AREA_UNIT", command.getTotalArea().getUnit())) throw new IllegalArgumentException("Invalid Total Area unit: " + command.getTotalArea().getUnit());
         // ... more validations for fields that are present in the command
     }
}