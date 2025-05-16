package com.thesss.platform.gateway.config;

import com.thesss.platform.gateway.filters.AuthenticationFilterFactory;
import com.thesss.platform.gateway.filters.ConsentCheckFilterFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class GatewayRouteConfig {

    @Value("${feature.toggle.enable-consent-filter-for-farmer-data-api:true}")
    private boolean enableConsentFilterForFarmerDataApi;

    // Define default resilience configuration names (should match application.yml)
    private static final String DEFAULT_CIRCUIT_BREAKER = "defaultCircuitBreaker";
    private static final String DEFAULT_RETRY = "defaultRetry";
    // Assuming a default rate limiter config is defined in YAML, e.g., 'defaultRateLimiter'
    // Or it can be configured per route with specific parameters if needed.

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                                           AuthenticationFilterFactory authFilter,
                                           ConsentCheckFilterFactory consentFilter) {
        // This config class is needed by the authFilter.apply method.
        // It should be defined within AuthenticationFilterFactory.java
        // For compilation, we assume it's: public static class Config {}
        AuthenticationFilterFactory.Config authFilterConfig = new AuthenticationFilterFactory.Config();

        // This config class is needed by the consentFilter.apply method.
        // It should be defined within ConsentCheckFilterFactory.java
        // For compilation, we assume: public static class Config { public Config(String scope){...} }
        // The actual data scope might be more dynamic or specific per API.
        ConsentCheckFilterFactory.Config consentFilterConfig = new ConsentCheckFilterFactory.Config("farmer_data:read");


        return builder.routes()
                // REQ-12-003: Route requests for Agricultural Statistics to the statistics service.
                .route("statistics-service-route", r -> r.path("/api/v1/statistics/**")
                        .and().method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE) // Example methods
                        .filters(f -> f.stripPrefix(2) // /api/v1 -> /
                                .filter(authFilter.apply(authFilterConfig)) // Apply API Key/Auth
                                .circuitBreaker(config -> config.setName(DEFAULT_CIRCUIT_BREAKER).setFallbackUri("forward:/fallback/statistics"))
                                .retry(config -> config.setRetries(3).setMethods(HttpMethod.GET))) // Example Retry for GET
                        .uri("lb://statistics-service"))

                // REQ-12-004: Route requests for Knowledge Base content to the query management service.
                // Path adjusted to /api/v1/knowledge-base/** as per REQ and OpenAPI
                .route("query-management-service-route", r -> r.path("/api/v1/knowledge-base/**")
                        .and().method(HttpMethod.GET)
                        .filters(f -> f.stripPrefix(2)
                                .filter(authFilter.apply(authFilterConfig))
                                .circuitBreaker(config -> config.setName(DEFAULT_CIRCUIT_BREAKER).setFallbackUri("forward:/fallback/knowledgebase"))
                                .retry(config -> config.setRetries(2).setMethods(HttpMethod.GET)))
                        .uri("lb://query-management-service"))

                // REQ-12-005: Route requests for Weather information to the weather service.
                .route("weather-service-route", r -> r.path("/api/v1/weather/**")
                        .and().method(HttpMethod.GET)
                        .filters(f -> f.stripPrefix(2)
                                .filter(authFilter.apply(authFilterConfig)) // Weather might just need API key
                                .circuitBreaker(config -> config.setName(DEFAULT_CIRCUIT_BREAKER).setFallbackUri("forward:/fallback/weather")))
                        .uri("lb://weather-service"))

                // REQ-12-006: Route requests for Farmer Consented Data to the farmer service.
                .route("farmer-service-route", r -> {
                    RouteLocatorBuilder.Builder routeBuilder = r.path("/api/v1/farmer-data/{farmerId}/**")
                            .and().method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE); // Example methods
                    routeBuilder.filters(f -> {
                        f.stripPrefix(2)
                         .filter(authFilter.apply(authFilterConfig)); // Requires API Key/Auth
                        if (enableConsentFilterForFarmerDataApi) {
                            f.filter(consentFilter.apply(consentFilterConfig)); // Apply Consent Check
                        }
                        f.circuitBreaker(config -> config.setName(DEFAULT_CIRCUIT_BREAKER).setFallbackUri("forward:/fallback/farmerdata"))
                         .retry(config -> config.setRetries(2).setMethods(HttpMethod.GET));
                        return f;
                    });
                    return routeBuilder.uri("lb://farmer-service");
                })
                // Example public API documentation route (if not handled by SecurityConfig alone)
                .route("openapi-docs", r -> r.path("/v3/api-docs/**", "/swagger-ui.html", "/webjars/swagger-ui/**", "/swagger-resources/**")
                        .filters(f -> f.stripPrefix(0)) // No strip if path is exact
                        .uri("http://localhost:${server.port}")) // Route to self for swagger, or configure properly
                .build();
    }

    // Example Fallback controller (should be in a different file, e.g. FallbackController.java)
    // For now, just showing how fallback URIs would be routed.
    // You would need a @RestController with @RequestMapping for these.
    // e.g. @RestController public class FallbackController {
    //          @GetMapping("/fallback/statistics") public Mono<String> statisticsFallback() { return Mono.just("Statistics service is currently unavailable."); }
    //      }
}