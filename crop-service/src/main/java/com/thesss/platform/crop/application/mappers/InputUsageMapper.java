package com.thesss.platform.crop.application.mappers;

import com.thesss.platform.crop.application.dtos.InputUsageDto;
import com.thesss.platform.crop.application.dtos.LogInputCommand;
import com.thesss.platform.crop.application.ports.outgoing.MasterDataServicePort;
import com.thesss.platform.crop.domain.model.InputUsage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring",
        uses = {MasterDataServicePort.class} /* MasterDataServicePort is injected directly */,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class InputUsageMapper {

    @Autowired
    protected MasterDataServicePort masterDataServicePort;

    @Mapping(target = "inputType", expression = "java(resolveMasterData(inputUsage.getInputTypeMasterId(), \"INPUT_TYPES\"))")
    @Mapping(target = "inputNameBrand", expression = "java(resolveMasterDataOrText(inputUsage.getInputNameBrandMasterIdOrText(), \"INPUT_NAMES\"))")
    @Mapping(target = "quantity", expression = "java(formatQuantityWithUnit(inputUsage.getQuantityValue(), inputUsage.getQuantityUnitMasterId(), \"UNITS\"))")
    @Mapping(target = "applicationMethod", expression = "java(resolveMasterData(inputUsage.getApplicationMethodMasterId(), \"APPLICATION_METHODS\"))")
    // Cost is mapped directly
    public abstract InputUsageDto toDto(InputUsage inputUsage);

    public abstract List<InputUsageDto> toDtoList(List<InputUsage> inputUsages);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "farmingActivity", ignore = true)
    public abstract InputUsage toDomain(LogInputCommand command);

    // Helper methods
    protected String resolveMasterData(String masterId, String category) {
        if (masterId == null) return null;
        return masterDataServicePort.resolveMasterDataValue(masterId, category);
    }

    protected String resolveMasterDataOrText(String masterIdOrText, String categoryIfMasterId) {
        if (masterIdOrText == null) return null;
        String resolvedValue = masterDataServicePort.resolveMasterDataValue(masterIdOrText, categoryIfMasterId);
        return resolvedValue != null ? resolvedValue : masterIdOrText;
    }

    protected String formatQuantityWithUnit(BigDecimal value, String unitMasterId, String unitCategory) {
        if (value == null || unitMasterId == null) return null;
        String unit = masterDataServicePort.resolveMasterDataValue(unitMasterId, unitCategory);
        return value.toPlainString() + " " + (unit != null ? unit : unitMasterId);
    }
}