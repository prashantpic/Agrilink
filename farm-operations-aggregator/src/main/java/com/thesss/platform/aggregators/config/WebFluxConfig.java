package com.thesss.platform.aggregators.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${app.cors.allowed-methods}")
    private String[] allowedMethods;

    @Value("${app.cors.allowed-headers}")
    private String[] allowedHeaders;

    @Value("${app.cors.max-age}")
    private long maxAge;


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Apply CORS to all /api/v1 paths
                .allowedOrigins(allowedOrigins) // Origins allowed to access, configure in application.yml
                .allowedMethods(allowedMethods) // HTTP methods allowed
                .allowedHeaders(allowedHeaders) // Headers allowed
                .allowCredentials(true)
                .maxAge(maxAge); // How long the results of a preflight request can be cached
    }

    // Example for customizing JSON codecs if needed:
    // @Override
    // public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
    //     ObjectMapper objectMapper = new ObjectMapper()
    //             .registerModule(new JavaTimeModule())
    //             .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    //             .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    //
    //     configurer.defaultCodecs().jackson2JsonEncoder(
    //             new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON)
    //     );
    //     configurer.defaultCodecs().jackson2JsonDecoder(
    //             new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON)
    //     );
    // }
}