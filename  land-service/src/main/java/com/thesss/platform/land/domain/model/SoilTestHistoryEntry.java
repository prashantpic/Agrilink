package com.thesss.platform.land.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a single soil test history entry.
 * REQ-2-012
 */
@Getter
@Setter(AccessLevel.PRIVATE) // Internal state changes controlled by methods
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA/MapStruct
public class SoilTestHistoryEntry {

    private UUID id;
    private LocalDate testDate;
    private NutrientLevel pH; // pH is a nutrient level conceptually
    private NutrientLevel organicCarbon;
    private NutrientLevel nitrogen; // N
    private NutrientLevel phosphorus; // P
    private NutrientLevel potassium; // K
    private String micronutrients; // Could be a structured VO or JSON string if complex
    private String testReportUrl; // URL to the document in Object Storage
    // AuditInfo might be relevant here
    // private AuditInfo auditInfo;

    public SoilTestHistoryEntry(UUID id, LocalDate testDate, NutrientLevel pH, NutrientLevel organicCarbon,
                                NutrientLevel nitrogen, NutrientLevel phosphorus, NutrientLevel potassium,
                                String micronutrients, String testReportUrl) {
        this.id = Objects.requireNonNull(id, "SoilTestHistoryEntry ID cannot be null.");
        this.testDate = Objects.requireNonNull(testDate, "Test date cannot be null.");
        // NutrientLevel VOs handle their own validation
        this.pH = pH; // Can be null if not tested
        this.organicCarbon = organicCarbon;
        this.nitrogen = nitrogen;
        this.phosphorus = phosphorus;
        this.potassium = potassium;
        this.micronutrients = micronutrients;
        this.testReportUrl = testReportUrl; // Validation of URL format/existence in App Service
    }

    public void updateDetails(LocalDate testDate, NutrientLevel pH, NutrientLevel organicCarbon,
                              NutrientLevel nitrogen, NutrientLevel phosphorus, NutrientLevel potassium,
                              String micronutrients, String testReportUrl) {
        this.testDate = Objects.requireNonNull(testDate, "Test date cannot be null.");
        this.pH = pH;
        this.organicCarbon = organicCarbon;
        this.nitrogen = nitrogen;
        this.phosphorus = phosphorus;
        this.potassium = potassium;
        this.micronutrients = micronutrients;
        this.testReportUrl = testReportUrl;
        // Update audit info if applicable
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SoilTestHistoryEntry that = (SoilTestHistoryEntry) o;
        return Objects.equals(id, that.id); // Entity equality based on ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Entity hashCode based on ID
    }
}