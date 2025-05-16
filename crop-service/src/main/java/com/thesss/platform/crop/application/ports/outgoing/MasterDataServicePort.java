package com.thesss.platform.crop.application.ports.outgoing;

import com.thesss.platform.crop.infrastructure.clients.dtos.MasterDataDto;
import java.math.BigDecimal;
import java.util.Optional;

public interface MasterDataServicePort {

    Optional<MasterDataDto> getMasterDataValue(String categoryKey, String itemKey);

    Optional<Integer> getCropDurationDays(String cropMasterId, String varietyMasterIdOrText);

    Optional<BigDecimal> getUnitConversionFactor(String fromUnitMasterId, String toUnitMasterId, String quantityType);

    String resolveMasterDataValue(String masterId, String category);
}