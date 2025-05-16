package com.thesss.platform.gateway.config;

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

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity // To enable method-level security like @PreAuthorize if needed
public class SecurityConfig {

    private static final String[] PUBLIC_API_DOCS_PATHS = {
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**", // For Swagger UI 3.x resources
            "/webjars/swagger-ui/**",
            "/swagger-resources/**",
            "/openapi/api-gateway-openapi.yml" // If served directly
    };

    private static final String[] PUBLIC_ACTUATOR_PATHS = {
            "/actuator/health/**",
            "/actuator/info"
    };

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable) // CSRF protection is typically not needed for stateless APIs
            .cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource())) // Apply CORS configuration
            .authorizeExchange(exchanges -> exchanges
                    .pathMatchers(PUBLIC_API_DOCS_PATHS).permitAll()
                    .pathMatchers(PUBLIC_ACTUATOR_PATHS).permitAll()
                    .pathMatchers("/actuator/**").authenticated() // Secure other actuator endpoints
                    .pathMatchers("/api/v1/**").authenticated() // REQ-12-001, REQ-12-002 (JWT part)
                    .anyExchange().authenticated() // Default deny
            )
            // REQ-12-002: Configure OAuth2 Resource Server for JWT validation
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
            // API Key validation is expected to be handled by AuthenticationFilterFactory at the route level.
            // If AuthenticationFilterFactory populates SecurityContext, this config might need adjustments.

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Configure allowed origins, methods, headers as per your requirements
        // Example: Allow all for development, restrict in production
        configuration.setAllowedOrigins(Arrays.asList("*")); // Or specific origins: "http://localhost:3000", "https://yourdomain.com"
        configuration.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name(),
                HttpMethod.PATCH.name()
        ));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-API-KEY",
                "X-Correlation-ID",
                "Origin",
                "Accept"
        ));
        configuration.setAllowCredentials(true); // If you need cookies or authorization headers with credentials
        configuration.setMaxAge(3600L); // How long the results of a preflight request can be cached

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}