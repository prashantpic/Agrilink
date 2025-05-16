package com.thesss.platform.crop.domain.services;

import com.thesss.platform.crop.domain.model.CultivationArea;
import com.thesss.platform.crop.domain.model.HarvestInformation;
import com.thesss.platform.crop.domain.model.YieldPerUnitArea;
import com.thesss.platform.crop.application.ports.outgoing.MasterDataServicePort;
import com.thesss.platform.crop.domain.exceptions.CalculationException;
import com.thesss.platform.crop.domain.exceptions.MasterDataResolutionException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Service
public class YieldCalculationService {

    private static final String STANDARD_YIELD_UNIT_FOR_CALC = "kg"; // Example standard unit key
    private static final String STANDARD_AREA_UNIT_FOR_CALC = "ha"; // Example standard unit key
    private static final int CALCULATION_SCALE = 6; // Precision for intermediate calculation
    private static final int RESULT_SCALE = 2; // Precision for final result

    public YieldPerUnitArea calculateYieldPerUnitArea(HarvestInformation harvestInfo, CultivationArea cultivationArea, MasterDataServicePort masterDataServicePort) {
        Objects.requireNonNull(harvestInfo, "Harvest information cannot be null");
        Objects.requireNonNull(cultivationArea, "Cultivation area cannot be null");
        Objects.requireNonNull(masterDataServicePort, "MasterDataServicePort cannot be null");

        BigDecimal totalYieldQuantity = harvestInfo.getTotalYieldQuantity();
        String yieldUnitMasterId = harvestInfo.getTotalYieldUnitMasterId();
        BigDecimal areaValue = cultivationArea.getAreaValue();
        String areaUnitMasterId = cultivationArea.getAreaUnitMasterId();

        if (totalYieldQuantity == null || yieldUnitMasterId == null || areaValue == null || areaUnitMasterId == null) {
             throw new CalculationException("Missing harvest or area data for yield calculation.");
        }
        if (areaValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CalculationException("Cultivated area must be positive for yield calculation.");
        }

        try {
            // Convert total yield to a standard unit (e.g., kilograms)
            BigDecimal yieldInStandardUnit = convertToStandardUnit(totalYieldQuantity, yieldUnitMasterId, STANDARD_YIELD_UNIT_FOR_CALC, "WEIGHT", masterDataServicePort);

            // Convert cultivated area to a standard unit (e.g., hectares)
            BigDecimal areaInStandardUnit = convertToStandardUnit(areaValue, areaUnitMasterId, STANDARD_AREA_UNIT_FOR_CALC, "AREA", masterDataServicePort);

            if (areaInStandardUnit.compareTo(BigDecimal.ZERO) == 0) {
                 throw new CalculationException("Converted cultivated area is zero, cannot calculate yield per unit area.");
            }

            BigDecimal yieldPerUnitAreaValue = yieldInStandardUnit.divide(areaInStandardUnit, CALCULATION_SCALE, RoundingMode.HALF_UP);
            BigDecimal finalValue = yieldPerUnitAreaValue.setScale(RESULT_SCALE, RoundingMode.HALF_UP);

            String derivedUnitString = masterDataServicePort.resolveMasterDataValue(STANDARD_YIELD_UNIT_FOR_CALC, "UNITS") +
                                     "/" +
                                     masterDataServicePort.resolveMasterDataValue(STANDARD_AREA_UNIT_FOR_CALC, "UNITS");


            return new YieldPerUnitArea(finalValue, derivedUnitString);

        } catch (MasterDataResolutionException e) {
            throw new CalculationException("Failed to resolve units or conversion factors from Master Data Service for yield calculation.", e);
        } catch (ArithmeticException e) {
            throw new CalculationException("Arithmetic error during yield calculation.", e);
        }
    }

    private BigDecimal convertToStandardUnit(BigDecimal value, String fromUnitMasterId, String toStandardUnitKey, String quantityType, MasterDataServicePort masterDataServicePort) {
        // In a real scenario, toStandardUnitKey would be a master data ID itself, e.g. "UNIT_KG_ID"
        // For this example, assume MasterDataServicePort.getUnitConversionFactor can handle key-to-key or id-to-id.
        // If fromUnitMasterId represents the standard unit already, no conversion needed.
        // This check needs more robust logic based on actual MasterData structure.
        // Let's assume a direct call to getUnitConversionFactor is sufficient.

        Optional<BigDecimal> conversionFactorOpt = masterDataServicePort.getUnitConversionFactor(fromUnitMasterId, toStandardUnitKey, quantityType);
        if (conversionFactorOpt.isEmpty()) {
            // If factor is 1 (same unit) or no factor needed, it might return empty or 1.
            // Let's assume if it's the same unit, the factor is 1.
            // This logic depends heavily on how MasterDataServicePort is implemented.
            // For now, if fromUnit represents the standard unit conceptually, return value.
            // This part needs refinement based on actual MasterDataServicePort contract.
            // A simple check for equality between `fromUnitMasterId` and a known ID for `toStandardUnitKey` would be better.
            if (isSameAsStandard(fromUnitMasterId, toStandardUnitKey, masterDataServicePort)) {
                 return value;
            }
            throw new MasterDataResolutionException("Conversion factor not found from " + fromUnitMasterId + " to standard " + quantityType + " unit '" + toStandardUnitKey + "'");
        }

        BigDecimal conversionFactor = conversionFactorOpt.get();
        return value.multiply(conversionFactor);
    }

    private boolean isSameAsStandard(String unitId, String standardUnitKey, MasterDataServicePort masterDataServicePort) {
        // This is a placeholder. In reality, you'd compare `unitId` with the
        // actual master data ID that `standardUnitKey` represents.
        // For example, if standardUnitKey is "kg", you'd check if unitId is the master ID for "kilogram".
        String resolvedUnit = masterDataServicePort.resolveMasterDataValue(unitId, "UNITS"); // Get display name
        return standardUnitKey.equalsIgnoreCase(resolvedUnit); // Simplistic check
    }
}