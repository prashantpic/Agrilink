package com.thesss.platform.land.application.port.out;

import java.util.List;

// This is a placeholder interface definition.
// Implementations will interact with the actual Master Data service via HTTP.
public interface MasterDataServicePort { // REQ-2-006, REQ-2-007, REQ-2-010, REQ-2-012, REQ-2-013, REQ-2-014, REQ-2-015, REQ-2-016, REQ-2-019

    /**
     * Validates if a given code is valid for a specific master data type.
     *
     * @param type The type of master data (e.g., "OwnershipType", "SoilType", "UnitOfMeasure").
     * @param code The code to validate.
     * @return true if the code is valid for the type, false otherwise.
     */
    boolean isValidCode(String type, String code);

    /**
     * Fetches the list of valid units of measure for a given category (e.g., "Area", "Nutrient").
     *
     * @param category The category of units (e.g., "Area", "Nutrient").
     * @return A list of valid unit codes.
     */
    // List<String> getValidUnits(String category); // Example method
}