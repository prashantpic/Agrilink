```java
package com.thesss.platform.land.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// Using @Configuration + @ConfigurationProperties or just @ConfigurationProperties
// @Configuration // If you want it to be a @Bean by default
@ConfigurationProperties(prefix = "app.land-service") // REPO-LAND-SVC, REQ-2-004, REQ-1.3-004
@Getter
@Setter
@Validated // Enable validation on these properties
public class AppProperties {

    @NotNull // Nested properties group should exist
    private GeospatialSettings geospatialSettings = new GeospatialSettings();

    @NotNull
    private ParcelIdValidation parcelIdValidation = new ParcelIdValidation();

    @NotNull
    private ExternalServiceEndpoints externalServiceEndpoints = new ExternalServiceEndpoints();


    @Getter
    @Setter
    public static class GeospatialSettings {
        @Min(0) // SRID is typically a positive integer
        private int defaultSridStorage = 4326; // REQ-2-020, REQ-1.3-012

        @NotBlank // Calculation SRID must be specified
        private String defaultSridCalculation = "EPSG:32630"; // REQ-2-006, REQ-1.3-004 - Placeholder, region-specific

        @DecimalMin("0.0")
        private double areaDiscrepancyThresholdPercentage = 10.0; // REQ-2-006, REQ-1.3-004
    }

    @Getter
    @Setter
    public static class ParcelIdValidation {
        @NotBlank
        private String uniquenessRegionCode = "DEFAULT_REGION"; // REQ-2-004
    }

    @Getter
    @Setter
    public static class ExternalServiceEndpoints {
        @NotBlank
        private String masterDataServiceBaseUrl;
        @NotBlank
        private String approvalWorkflowServiceBaseUrl;
        @NotBlank
        private String cropServiceBaseUrl;
        @NotNull
        private ObjectStorageService objectStorageService = new ObjectStorageService();

        @Getter
        @Setter
        public static class ObjectStorageService {
            @NotBlank
            private String bucketName = "land-documents";
            // Add other object storage specific properties if needed (e.g., endpoint for MinIO)
            // private String endpoint;
            // private String accessKey;
            // private String secretKey;
        }
    }
}
```