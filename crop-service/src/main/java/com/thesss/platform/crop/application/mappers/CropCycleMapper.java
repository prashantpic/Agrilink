package com.thesss.platform.crop.application.mappers;

import com.thesss.platform.crop.application.dtos.*;
import com.thesss.platform.crop.application.ports.outgoing.MasterDataServicePort;
import com.thesss.platform.crop.domain.model.*;
import com.thesss.platform.crop.domain.model.statemachine.CropCycleState; // For initial status
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List; // For List mapping methods
import java.util.UUID; // For CropCycleId

@Mapper(componentModel = "spring",
        uses = {FarmingActivityMapper.class, MarketSaleMapper.class /* MasterDataServicePort is injected */},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class CropCycleMapper {

    // Keep MasterDataServicePort injected for custom resolver methods
    @Autowired
    protected MasterDataServicePort masterDataServicePort;

    @Autowired
    protected FarmingActivityMapper farmingActivityMapper;

    @Autowired
    protected MarketSaleMapper marketSaleMapper;


    @Mapping(target = "cropCycleBusinessId", source = "cropCycle.cropCycleId.value")
    @Mapping(target = "cropName", expression = "java(resolveMasterData(cropCycle.getCropDetails() != null ? cropCycle.getCropDetails().getCropNameMasterId() : null, \"CROPS\"))")
    @Mapping(target = "cropVariety", expression = "java(resolveMasterDataOrText(cropCycle.getCropDetails() != null ? cropCycle.getCropDetails().getCropVarietyMasterIdOrText() : null, \"CROP_VARIETIES\"))")
    @Mapping(target = "season", expression = "java(resolveMasterData(cropCycle.getCropDetails() != null ? cropCycle.getCropDetails().getSeasonMasterId() : null, \"SEASONS\"))")
    @Mapping(target = "cultivationYear", source = "cropCycle.cropDetails.cultivationYear")
    @Mapping(target = "plannedSowingDate", source = "cropCycle.sowingInformation.plannedSowingDate")
    @Mapping(target = "actualSowingDate", source = "cropCycle.sowingInformation.actualSowingDate")
    @Mapping(target = "expectedHarvestDate", source = "cropCycle.sowingInformation.expectedHarvestDate")
    @Mapping(target = "actualHarvestDate", source = "cropCycle.harvestInformation.actualHarvestDate")
    @Mapping(target = "seedingRate", expression = "java(formatQuantityWithUnit(cropCycle.getSowingInformation() != null ? cropCycle.getSowingInformation().getSeedingRateValue() : null, cropCycle.getSowingInformation() != null ? cropCycle.getSowingInformation().getSeedingRateUnitMasterId() : null, \"UNITS\"))")
    @Mapping(target = "seedSource", expression = "java(resolveMasterDataOrText(cropCycle.getSowingInformation() != null ? cropCycle.getSowingInformation().getSeedSourceMasterIdOrText() : null, \"SEED_SOURCES\"))")
    @Mapping(target = "cultivatedArea", expression = "java(formatQuantityWithUnit(cropCycle.getCultivatedArea() != null ? cropCycle.getCultivatedArea().getAreaValue() : null, cropCycle.getCultivatedArea() != null ? cropCycle.getCultivatedArea().getAreaUnitMasterId() : null, \"UNITS\"))")
    @Mapping(target = "status", expression = "java(resolveMasterData(cropCycle.getStatusInfo() != null ? cropCycle.getStatusInfo().getStatusMasterId() : null, \"CROP_STATUSES\"))")
    @Mapping(target = "reasonForFailure", source = "cropCycle.statusInfo.reasonForFailure")
    @Mapping(target = "totalYield", expression = "java(formatQuantityWithUnit(cropCycle.getHarvestInformation() != null ? cropCycle.getHarvestInformation().getTotalYieldQuantity() : null, cropCycle.getHarvestInformation() != null ? cropCycle.getHarvestInformation().getTotalYieldUnitMasterId() : null, \"UNITS\"))")
    @Mapping(target = "qualityGrade", expression = "java(resolveMasterData(cropCycle.getHarvestInformation() != null ? cropCycle.getHarvestInformation().getQualityGradeMasterId() : null, \"QUALITY_GRADES\"))")
    @Mapping(target = "yieldPerUnitArea", source = "yieldPerUnitArea", qualifiedByName = "formatYieldPerUnitAreaToString")
    @Mapping(target = "activities", source = "cropCycle.activities") // Delegate to FarmingActivityMapper
    @Mapping(target = "marketSales", source = "cropCycle.marketSales") // Delegate to MarketSaleMapper
    public abstract CropCycleDto toDto(CropCycle cropCycle, YieldPerUnitArea yieldPerUnitArea);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cropCycleId", expression = "java(new com.thesss.platform.crop.domain.model.CropCycleId(java.util.UUID.randomUUID()))")
    @Mapping(target = "cropDetails", source = "command", qualifiedByName = "commandToCropDetails")
    @Mapping(target = "sowingInformation", source = "command", qualifiedByName = "commandToSowingInformation")
    @Mapping(target = "harvestInformation", ignore = true)
    @Mapping(target = "cultivatedArea", source = "command", qualifiedByName = "commandToCultivationArea")
    @Mapping(target = "statusInfo", expression = "java(new com.thesss.platform.crop.domain.model.CropCycleStatusInfo(com.thesss.platform.crop.domain.model.statemachine.CropCycleState.PLANNED.name(), null))")
    @Mapping(target = "notes", source = "notes")
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "marketSales", ignore = true)
    @Mapping(target = "recordCreationDate", ignore = true)
    @Mapping(target = "lastUpdatedDate", ignore = true)
    public abstract CropCycle toDomain(CreateCropCycleCommand command);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cropCycleId", ignore = true)
    @Mapping(target = "farmerId", source = "farmerId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "landRecordId", source = "landRecordId", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "cropDetails", source = "command", qualifiedByName = "updateCropDetailsFromCommand")
    @Mapping(target = "sowingInformation", source = "command", qualifiedByName = "updateSowingInformationFromCommand")
    @Mapping(target = "cultivatedArea", source = "command", qualifiedByName = "updateCultivationAreaFromCommand")
    @Mapping(target = "notes", source = "notes", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "harvestInformation", ignore = true)
    @Mapping(target = "statusInfo", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "marketSales", ignore = true)
    @Mapping(target = "recordCreationDate", ignore = true)
    @Mapping(target = "lastUpdatedDate", ignore = true)
    public abstract void updateDomainFromCommand(UpdateCropCycleCoreInfoCommand command, @MappingTarget CropCycle cropCycle);


    // Helper methods for custom mapping logic
    protected String resolveMasterData(String masterId, String category) {
        if (masterId == null) return null;
        return masterDataServicePort.resolveMasterDataValue(masterId, category);
    }

    protected String resolveMasterDataOrText(String masterIdOrText, String categoryIfMasterId) {
        if (masterIdOrText == null) return null;
        String resolvedValue = masterDataServicePort.resolveMasterDataValue(masterIdOrText, categoryIfMasterId);
        return resolvedValue != null ? resolvedValue : masterIdOrText; // Fallback to text if not resolved as ID
    }

    protected String formatQuantityWithUnit(BigDecimal value, String unitMasterId, String unitCategory) {
        if (value == null || unitMasterId == null) return null;
        String unit = masterDataServicePort.resolveMasterDataValue(unitMasterId, unitCategory);
        return value.toPlainString() + " " + (unit != null ? unit : unitMasterId);
    }

    @Named("formatYieldPerUnitAreaToString")
    protected String formatYieldPerUnitAreaToString(YieldPerUnitArea yield) {
        return yield != null ? yield.toString() : null;
    }

    @Named("commandToCropDetails")
    protected CropDetails commandToCropDetails(CreateCropCycleCommand command) {
        return new CropDetails(command.getCropNameMasterId(), command.getCropVarietyMasterIdOrText(), command.getSeasonMasterId(), command.getCultivationYear());
    }

    @Named("commandToSowingInformation")
    protected SowingInformation commandToSowingInformation(CreateCropCycleCommand command) {
        // Expected harvest date will be calculated by the application service
        return new SowingInformation(command.getPlannedSowingDate(), command.getActualSowingDate(), null, command.getSeedingRateValue(), command.getSeedingRateUnitMasterId(), command.getSeedSourceMasterIdOrText());
    }

    @Named("commandToCultivationArea")
    protected CultivationArea commandToCultivationArea(CreateCropCycleCommand command) {
        return new CultivationArea(command.getCultivatedAreaValue(), command.getCultivatedAreaUnitMasterId());
    }

    // For updates, we need to handle existing values if command fields are null
    @Named("updateCropDetailsFromCommand")
    protected CropDetails updateCropDetailsFromCommand(UpdateCropCycleCoreInfoCommand command, @Context CropCycle existingCropCycle) {
        CropDetails current = existingCropCycle.getCropDetails();
        return new CropDetails(
            command.getCropNameMasterId() != null ? command.getCropNameMasterId() : current.getCropNameMasterId(),
            command.getCropVarietyMasterIdOrText() != null ? command.getCropVarietyMasterIdOrText() : current.getCropVarietyMasterIdOrText(),
            command.getSeasonMasterId() != null ? command.getSeasonMasterId() : current.getSeasonMasterId(),
            command.getCultivationYear() != null ? command.getCultivationYear() : current.getCultivationYear()
        );
    }

    @Named("updateSowingInformationFromCommand")
    protected SowingInformation updateSowingInformationFromCommand(UpdateCropCycleCoreInfoCommand command, @Context CropCycle existingCropCycle) {
        SowingInformation current = existingCropCycle.getSowingInformation();
        // Expected harvest date update is handled by app service prediction logic, or user override
        return new SowingInformation(
            command.getPlannedSowingDate() != null ? command.getPlannedSowingDate() : current.getPlannedSowingDate(),
            command.getActualSowingDate() != null ? command.getActualSowingDate() : current.getActualSowingDate(),
            command.getExpectedHarvestDate() != null ? command.getExpectedHarvestDate() : current.getExpectedHarvestDate(),
            command.getSeedingRateValue() != null ? command.getSeedingRateValue() : current.getSeedingRateValue(),
            command.getSeedingRateUnitMasterId() != null ? command.getSeedingRateUnitMasterId() : current.getSeedingRateUnitMasterId(),
            command.getSeedSourceMasterIdOrText() != null ? command.getSeedSourceMasterIdOrText() : current.getSeedSourceMasterIdOrText()
        );
    }

    @Named("updateCultivationAreaFromCommand")
    protected CultivationArea updateCultivationAreaFromCommand(UpdateCropCycleCoreInfoCommand command, @Context CropCycle existingCropCycle) {
        CultivationArea current = existingCropCycle.getCultivatedArea();
        return new CultivationArea(
            command.getCultivatedAreaValue() != null ? command.getCultivatedAreaValue() : current.getAreaValue(),
            command.getCultivatedAreaUnitMasterId() != null ? command.getCultivatedAreaUnitMasterId() : current.getAreaUnitMasterId()
        );
    }
}