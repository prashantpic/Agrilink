package com.thesss.platform.discovery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for securing the Eureka Service Discovery server.
 * This class configures authentication and authorization for accessing Eureka server resources,
 * including its dashboard and API endpoints. It also ensures that Spring Boot Actuator
 * endpoints, critical for APM (as per REQ-17-005), are appropriately secured.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain for HTTP requests.
     * <p>
     * It enables HTTP Basic authentication for all requests and disables CSRF protection,
     * which is common for service-to-service communication as with Eureka clients.
     * All endpoints, including Eureka dashboard/API (`/`, `/eureka/**`) and Actuator
     * endpoints (`/actuator/**`), require authentication.
     * </p>
     *
     * @param http The {@link HttpSecurity} to configure.
     * @return The configured {@link SecurityFilterChain}.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    // Secure Eureka dashboard, API, and Actuator endpoints.
                    // All other requests also require authentication.
                    .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults()) // Use HTTP Basic authentication
            .sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Eureka is typically stateless
            )
            .csrf(AbstractHttpConfigurer::disable); // Disable CSRF, common for Eureka servers and APIs

        return http.build();
    }
}