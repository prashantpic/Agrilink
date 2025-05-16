package com.thesss.platform.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.security.Principal;

// If using specific discovery client like Eureka, @EnableEurekaClient can be used.
// @EnableDiscoveryClient is more generic.
@EnableDiscoveryClient
@SpringBootApplication
public class GatewayApplication {

    /**
     * Main method to launch the Spring Boot application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    // Example KeyResolver beans - these should ideally be in a config class
    // but for demonstration, can be here or in application.yml with SpEL if simple.
    // These are referenced in application.yml.
    // In a real application, these would be more sophisticated.

    /**
     * A KeyResolver that uses the remote IP address of the client.
     * Referenced as "#{@remoteAddrKeyResolver}" in application.yml.
     */
    @Bean(name = "remoteAddrKeyResolver")
    public KeyResolver remoteAddrKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }

    /**
     * A KeyResolver that attempts to use an API key from a custom header
     * or the principal name if the user is authenticated.
     * Referenced as "#{@apiKeyOrPrincipalKeyResolver}" in application.yml.
     * This is a simplified example; a real implementation would involve more robust logic
     * for API key extraction and validation context.
     */
    @Bean(name = "apiKeyOrPrincipalKeyResolver")
    public KeyResolver apiKeyOrPrincipalKeyResolver() {
        // This is a placeholder. Actual API Key logic might be in AuthenticationFilterFactory
        // or a more dedicated KeyResolver that checks request attributes set by an auth filter.
        // For JWT authenticated requests, principal name is a good candidate.
        // For API Key, it might be the client_id associated with the key.
        String apiKeyHeaderName = "X-API-KEY"; // Should be configurable, e.g., from application.yml

        return exchange -> exchange.getPrincipal()
                .map(Principal::getName)
                .switchIfEmpty(Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst(apiKeyHeaderName)))
                .defaultIfEmpty("anonymous"); // Fallback if no principal and no API key header
    }

    // Fallback controller placeholder - should be in its own controller class
    // For CircuitBreaker fallback URIs in application.yml
    // @RestController
    // public static class FallbackController {
    //     @GetMapping("/fallback/statistics")
    //     public Mono<ResponseEntity<Map<String, String>>> statisticsFallback() {
    //         Map<String, String> fallbackResponse = new HashMap<>();
    //         fallbackResponse.put("error", "Statistics service is temporarily unavailable.");
    //         fallbackResponse.put("message", "Please try again later.");
    //         return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallbackResponse));
    //     }
    //     // Other fallbacks ...
    // }
}

// Define KeyResolver interface if not using Spring Cloud Gateway's own.
// Spring Cloud Gateway provides io.spring.cloud.gateway.filter.ratelimit.KeyResolver
interface KeyResolver extends org.springframework.cloud.gateway.filter.ratelimit.KeyResolver {}

// Placeholder for BusinessValidationException, typically in an 'exception' package
// package com.thesss.platform.gateway.exception;
// public class BusinessValidationException extends RuntimeException {
//     public BusinessValidationException(String message) {
//         super(message);
//     }
// }