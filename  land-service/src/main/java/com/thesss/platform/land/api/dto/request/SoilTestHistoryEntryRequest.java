package com.thesss.platform.land.api.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data // REQ-2-012
@Builder
public class SoilTestHistoryEntryRequest {

    // Optional ID for updates, not present for creation
    private UUID id;

    @NotNull(message = "Test date is required.")
    @PastOrPresent(message = "Test date must be in the past or present.")
    private LocalDate testDate;

    @DecimalMin(value = "0.0", message = "pH value must be non-negative.")
    @DecimalMax(value = "14.0", message = "pH value must not exceed 14.")
    private Double pH;

    @Valid
    private NutrientRequest nitrogen; // N

    @Valid
    private NutrientRequest phosphorus; // P

    @Valid
    private NutrientRequest potassium; // K

    @Size(max = 255, message = "Micronutrients description must not exceed 255 characters.")
    private String micronutrients; // Free text or codes

    @Size(max = 2048, message = "Test report URL must not exceed 2048 characters.")
    // @Pattern(regexp = "URL_REGEX_HERE", message = "Invalid URL format for test report.") // Add URL pattern if needed
    private String testReportUrl; // URL reference to object storage report

    // Nested DTO for Nutrient Levels
    @Data
    @Builder
    public static class NutrientRequest {
        @NotNull(message = "Nutrient value is required.")
        @DecimalMin(value = "0.0", message = "Nutrient value must be non-negative.")
        private Double value;

        @NotBlank(message = "Nutrient unit is required.")
        @Size(max = 20, message = "Nutrient unit code must not exceed 20 characters.")
        private String unit; // Code from Master Data
    }
}