package com.thesss.platform.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity // Enables @PreAuthorize, @PostAuthorize, etc.
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri; // Used to demonstrate JWT configuration, actual validation uses properties

    @Value("${app.cors.allowed-origins:*}") // Default to all, should be restricted in production
    private List<String> allowedOrigins;

    private static final String[] PUBLIC_API_DOCS_PATHS = {
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/swagger-ui/**",
            "/openapi/api-gateway-openapi.yml" // If served directly
    };

    private static final String[] PUBLIC_ACTUATOR_PATHS = {
            "/actuator/health/**",
            "/actuator/info"
            // Prometheus endpoint should be secured or restricted
    };


    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable) // CSRF not needed for stateless API Gateway
            .cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource()))
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(PUBLIC_API_DOCS_PATHS).permitAll()
                .pathMatchers(PUBLIC_ACTUATOR_PATHS).permitAll()
                .pathMatchers(HttpMethod.OPTIONS).permitAll() // Allow CORS preflight requests
                .pathMatchers("/api/v1/**").authenticated() // REQ-12-001, REQ-12-002 (partially for JWT)
                .anyExchange().denyAll() // Deny by default
            )
            // REQ-12-002: Secure public API access using JWT validation
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
            // Customizer.withDefaults() will pick up issuer-uri or jwk-set-uri from application properties.
            // For API Key validation, it's assumed to be handled by AuthenticationFilterFactory.
            // If API Key sets a Principal, then it might interact here.

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins); // Configure allowed origins from properties
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-API-KEY", "X-Correlation-ID"));
        configuration.setAllowCredentials(true); // Important if cookies or auth headers are expected
        configuration.setMaxAge(3600L); // Cache preflight response for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // If API key validation needs to integrate directly with Spring Security's authentication flow
    // (beyond just a filter that conditionally passes/fails requests),
    // you might define a custom ReactiveAuthenticationManager or ServerAuthenticationConverter.
    // However, the SDS implies AuthenticationFilterFactory handles API key validation.
    /*
    @Bean
    public ReactiveAuthenticationManager apiKeyAuthenticationManager(AuthServiceClient authServiceClient,
                                                                    @Value("${feature-flags.use-remote-auth-service-for-api-keys:true}") boolean useRemote) {
        // Example: return new ApiKeyReactiveAuthenticationManager(authServiceClient, useRemote);
        return authentication -> Mono.empty(); // Placeholder
    }

    @Bean
    ServerAuthenticationConverter apiKeyAuthenticationConverter() {
        return exchange -> {
            // Logic to extract API key from request (e.g., X-API-KEY header)
            // List<String> apiKeyHeaders = exchange.getRequest().getHeaders().get("X-API-KEY");
            // if (apiKeyHeaders != null && !apiKeyHeaders.isEmpty()) {
            //    String apiKey = apiKeyHeaders.get(0);
            //    return Mono.just(new ApiKeyAuthenticationToken(apiKey)); // Custom AuthenticationToken
            // }
            return Mono.empty();
        };
    }
    */
}