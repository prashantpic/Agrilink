package com.thesss.platform.gateway.config;

import com.thesss.platform.gateway.filters.AuthenticationFilterFactory;
import com.thesss.platform.gateway.filters.ConsentCheckFilterFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.util.List;

@Configuration
public class GatewayRouteConfig {

    @Value("${feature-flags.enable-consent-filter-for-farmer-data-api:true}")
    private boolean enableConsentFilterForFarmerDataApi;

    // Placeholder Config classes for filter factories
    // These would normally be defined within their respective FilterFactory classes.
    // Included here to make this GatewayRouteConfig class's logic more self-contained for this example.

    public static class AuthenticationFilterConfig {
        private List<String> requiredScopes;

        public AuthenticationFilterConfig() {}

        public AuthenticationFilterConfig(List<String> requiredScopes) {
            this.requiredScopes = requiredScopes;
        }

        public List<String> getRequiredScopes() {
            return requiredScopes;
        }

        public void setRequiredScopes(List<String> requiredScopes) {
            this.requiredScopes = requiredScopes;
        }
    }

    public static class ConsentFilterConfig {
        private String dataScope; // Example: "farm_details", "crop_history"

        public ConsentFilterConfig() {}

        public ConsentFilterConfig(String dataScope) {
            this.dataScope = dataScope;
        }

        public String getDataScope() {
            return dataScope;
        }

        public void setDataScope(String dataScope) {
            this.dataScope = dataScope;
        }
    }


    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder,
                                           AuthenticationFilterFactory authenticationFilterFactory,
                                           ConsentCheckFilterFactory consentCheckFilterFactory) {
        return builder.routes()
                // REQ-12-003: Route requests for Agricultural Statistics to the statistics service.
                .route("statistics-service-route", r -> r.path("/api/v1/statistics/**")
                        .and().method(HttpMethod.GET, HttpMethod.POST) // Example methods
                        .filters(f -> f.stripPrefix(3) // /api/v1/statistics -> /
                                .filter(authenticationFilterFactory.apply(new AuthenticationFilterConfig(List.of("statistics:read"))))
                                .retry(retryConfig -> retryConfig.setRetries(3)
                                        .setMethods(HttpMethod.GET, HttpMethod.POST)
                                        // Add other retry configurations if needed from application.yml or defaults
                                )
                                .circuitBreaker(cbConfig -> cbConfig.setName("statistics-service-cb")
                                        .setFallbackUri("forward:/fallback/statistics"))
                                // Assuming RequestRateLimiter is configured globally or with a specific key resolver
                                .requestRateLimiter(config -> config.setKeyResolver("#{@customKeyResolver}").setRateLimiter("#{@customRateLimiter}"))
                        )
                        .uri("lb://statistics-service"))

                // REQ-12-004: Route requests for Knowledge Base content to the query management service.
                .route("query-management-service-route", r -> r.path("/api/v1/knowledge-base/**")
                        .and().method(HttpMethod.GET)
                        .filters(f -> f.stripPrefix(3) // /api/v1/knowledge-base -> /
                                .filter(authenticationFilterFactory.apply(new AuthenticationFilterConfig(List.of("kb:read"))))
                                .retry(retryConfig -> retryConfig.setRetries(3).setMethods(HttpMethod.GET))
                                .circuitBreaker(cbConfig -> cbConfig.setName("query-management-service-cb")
                                        .setFallbackUri("forward:/fallback/knowledgebase"))
                                .requestRateLimiter(config -> config.setKeyResolver("#{@customKeyResolver}").setRateLimiter("#{@customRateLimiter}"))
                        )
                        .uri("lb://query-management-service"))

                // REQ-12-005: Route requests for Weather information to the weather service.
                .route("weather-service-route", r -> r.path("/api/v1/weather/**")
                        .and().method(HttpMethod.GET)
                        .filters(f -> f.stripPrefix(3) // /api/v1/weather -> /
                                .filter(authenticationFilterFactory.apply(new AuthenticationFilterConfig(List.of("weather:read"))))
                                .retry(retryConfig -> retryConfig.setRetries(3).setMethods(HttpMethod.GET))
                                .circuitBreaker(cbConfig -> cbConfig.setName("weather-service-cb")
                                        .setFallbackUri("forward:/fallback/weather"))
                                .requestRateLimiter(config -> config.setKeyResolver("#{@customKeyResolver}").setRateLimiter("#{@customRateLimiter}"))
                        )
                        .uri("lb://weather-service"))

                // REQ-12-006: Route requests for Farmer Consented Data to the farmer service.
                .route("farmer-service-route", r -> r.path("/api/v1/farmer-data/{farmerId}/**")
                        .and().method(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST) // Example methods
                        .filters(f -> {
                            f.stripPrefix(4); // /api/v1/farmer-data/{farmerId} -> / (adjust if farmerId is part of downstream path)
                            f.filter(authenticationFilterFactory.apply(new AuthenticationFilterConfig(List.of("farmer_data:read", "farmer_data:write"))));
                            if (enableConsentFilterForFarmerDataApi) {
                                // Assuming dataScope might be dynamic or a default; here, a generic one.
                                // In a real scenario, dataScope might be extracted or configured more dynamically.
                                f.filter(consentCheckFilterFactory.apply(new ConsentFilterConfig("farmer_profile_access")));
                            }
                            f.retry(retryConfig -> retryConfig.setRetries(2).setMethods(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST));
                            f.circuitBreaker(cbConfig -> cbConfig.setName("farmer-service-cb")
                                    .setFallbackUri("forward:/fallback/farmerdata"));
                            f.requestRateLimiter(config -> config.setKeyResolver("#{@customKeyResolver}").setRateLimiter("#{@customRateLimiter}"));
                            return f;
                        })
                        .uri("lb://farmer-service"))
                
                // Fallback controller route if not defined via @RestController
                // .route("fallback-route", r -> r.path("/fallback/**")
                //    .uri("lb://api-gateway")) // Or point to a local controller
                .build();
    }

    // Example fallback controller (can be a separate @RestController)
    // For simplicity, could be added if not using forward:/ to a specific service that handles fallbacks
    /*
    @RestController
    public static class FallbackController {
        private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

        @RequestMapping("/fallback/statistics")
        public Mono<ResponseEntity<String>> statisticsFallback(ServerWebExchange exchange, Throwable throwable) {
            log.warn("Fallback for statistics service triggered. Error: {}", throwable.getMessage());
            // You can get correlation ID from exchange.getAttribute("correlationId")
            return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                             .body("Statistics service is temporarily unavailable. Please try again later."));
        }
        // Other fallback methods...
    }
    */

    // Example KeyResolver and RateLimiter beans (must be defined for requestRateLimiter filter)
    // These would typically be in a separate config or defined based on `spring.cloud.gateway.redis-rate-limiter` properties
    /*
    @Bean
    public KeyResolver customKeyResolver() {
        // Example: return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
        // Or based on user principal if authenticated:
        return exchange -> exchange.getPrincipal().flatMap(p -> Mono.just(p.getName())).defaultIfEmpty("anonymous");
    }

    @Bean
    public RateLimiter customRateLimiter(ReactiveRedisTemplate<String, String> redisTemplate,
                                        @Qualifier(RedisRateLimiter.REDIS_SCRIPT_NAME) RedisScript<List<Long>> script,
                                        Validator validator) {
        // This uses the default RedisRateLimiter configuration.
        // You can customize configurations (replenishRate, burstCapacity) per KeyResolver or globally.
        // Configuration would typically come from application.yml (spring.cloud.gateway.redis-rate-limiter.replenish-rate etc.)
        return new RedisRateLimiter(redisTemplate, script, validator);
    }
    */
}