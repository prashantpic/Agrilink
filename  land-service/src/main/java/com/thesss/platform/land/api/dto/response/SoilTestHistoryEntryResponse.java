package com.thesss.platform.land.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data // REQ-2-012
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SoilTestHistoryEntryResponse {
    private UUID id;
    private LocalDate testDate;
    private Double pH;
    private NutrientResponse nitrogen;
    private NutrientResponse phosphorus;
    private NutrientResponse potassium;
    private String micronutrients;
    private String testReportUrl;

    @Data
    @Builder
    public static class NutrientResponse {
        private Double value;
        private String unit;
    }
}