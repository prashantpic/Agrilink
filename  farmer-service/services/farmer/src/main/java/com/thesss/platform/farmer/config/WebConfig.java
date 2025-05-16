package com.thesss.platform.farmer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// import org.locationtech.jts.geom.GeometryFactory;
// import org.locationtech.jts.geom.PrecisionModel;
// import org.n52.jackson.datatype.jts.JtsModule;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Adjust path pattern as needed
                .allowedOrigins("*") // Configure allowed origins appropriately for production
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false); // Set to true if credentials (cookies, auth headers) are needed
    }

    // Example for customizing ObjectMapper, e.g., for JTS types if directly used in DTOs
    // (Though the SDS suggests using Latitude/Longitude DTO fields, which is generally simpler for APIs)
    /*
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // For Java 8 Date/Time

        // Configure JTS Module for PostGIS Geometry types if you expose them directly in DTOs
        // SRID 4326 is WGS 84, common for GPS coordinates
        // GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        // JtsModule jtsModule = new JtsModule(geometryFactory);
        // objectMapper.registerModule(jtsModule);

        return objectMapper;
    }
    */
}