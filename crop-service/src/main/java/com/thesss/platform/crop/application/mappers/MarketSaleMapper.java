package com.thesss.platform.crop.application.mappers;

import com.thesss.platform.crop.application.dtos.AddMarketSaleCommand;
import com.thesss.platform.crop.application.dtos.MarketSaleDto;
import com.thesss.platform.crop.application.ports.outgoing.MasterDataServicePort;
import com.thesss.platform.crop.domain.model.MarketSale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring",
        uses = {MasterDataServicePort.class} /* MasterDataServicePort is injected directly */,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class MarketSaleMapper {

    @Autowired
    protected MasterDataServicePort masterDataServicePort;

    @Mapping(target = "quantitySold", expression = "java(formatQuantityWithUnit(marketSale.getQuantitySoldValue(), marketSale.getQuantitySoldUnitMasterId(), \"UNITS\"))")
    // Other fields (salePricePerUnit, buyerNameOrMarket, saleDate) are mapped directly
    public abstract MarketSaleDto toDto(MarketSale marketSale);

    public abstract List<MarketSaleDto> toDtoList(List<MarketSale> marketSales);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cropCycle", ignore = true)
    public abstract MarketSale toDomain(AddMarketSaleCommand command);

    // Helper method
    protected String formatQuantityWithUnit(BigDecimal value, String unitMasterId, String unitCategory) {
        if (value == null || unitMasterId == null) return null;
        String unit = masterDataServicePort.resolveMasterDataValue(unitMasterId, unitCategory);
        return value.toPlainString() + " " + (unit != null ? unit : unitMasterId);
    }
}