package com.thesss.platform.crop.application.mappers;

import com.thesss.platform.crop.application.dtos.AddFarmingActivityCommand;
import com.thesss.platform.crop.application.dtos.FarmingActivityDto;
import com.thesss.platform.crop.application.ports.outgoing.MasterDataServicePort;
import com.thesss.platform.crop.domain.model.FarmingActivity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring",
        uses = {InputUsageMapper.class /* MasterDataServicePort is injected */},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class FarmingActivityMapper {

    @Autowired
    protected MasterDataServicePort masterDataServicePort;

    @Autowired
    protected InputUsageMapper inputUsageMapper; // For List<InputUsageDto>

    @Mapping(target = "activityType", expression = "java(resolveMasterData(activity.getActivityTypeMasterId(), \"ACTIVITY_TYPES\"))")
    @Mapping(target = "laborUsed", expression = "java(formatQuantityWithUnit(activity.getLaborUsedValue(), activity.getLaborUsedUnitMasterId(), \"UNITS\"))")
    @Mapping(target = "inputsUsed", source = "inputsUsed") // Delegate to InputUsageMapper for list
    public abstract FarmingActivityDto toDto(FarmingActivity activity);

    public abstract List<FarmingActivityDto> toDtoList(List<FarmingActivity> activities);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cropCycle", ignore = true)
    @Mapping(target = "inputsUsed", ignore = true) // Inputs are added via separate commands
    public abstract FarmingActivity toDomain(AddFarmingActivityCommand command);

    // Helper methods
    protected String resolveMasterData(String masterId, String category) {
        if (masterId == null) return null;
        return masterDataServicePort.resolveMasterDataValue(masterId, category);
    }

    protected String formatQuantityWithUnit(BigDecimal value, String unitMasterId, String unitCategory) {
        if (value == null || unitMasterId == null) return null;
        String unit = masterDataServicePort.resolveMasterDataValue(unitMasterId, unitCategory);
        return value.toPlainString() + " " + (unit != null ? unit : unitMasterId);
    }
}