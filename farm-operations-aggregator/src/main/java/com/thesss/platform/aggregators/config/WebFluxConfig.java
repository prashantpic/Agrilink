package com.thesss.platform.aggregators.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    @Value("${app.cors.allowed-origins:*}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Define the path pattern for CORS
                .allowedOrigins(allowedOrigins) // Allowed origins (e.g., "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed HTTP methods
                .allowedHeaders("*") // Allowed headers
                .allowCredentials(true) // Allow credentials
                .maxAge(3600); // Max age for pre-flight requests
    }

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        // Customize codecs if needed, e.g., for non-default JSON handling
        // By default, Spring WebFlux uses Jackson for JSON, which is usually sufficient.
        // Example for custom Jackson settings (if needed):
        // ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
        // .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        // .build();
        // configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
        // configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
    }
}