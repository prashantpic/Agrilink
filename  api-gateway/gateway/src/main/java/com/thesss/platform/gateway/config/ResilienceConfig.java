package com.thesss.platform.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import io.github.resilience4j.common.retry.configuration.RetryConfigCustomizer;
import io.github.resilience4j.common.timelimiter.configuration.TimeLimiterConfigCustomizer;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    /**
     * Provides default configurations for CircuitBreakers.
     * These can be overridden by instance-specific configurations in application.yml.
     * (e.g., resilience4j.circuitbreaker.instances.myBackend.slidingWindowSize=10)
     */
    @Bean
    public CircuitBreakerConfigCustomizer defaultCircuitBreakerConfigCustomizer() {
        return CircuitBreakerConfigCustomizer
                .of("default", builder -> builder
                        .failureRateThreshold(50) // If 50% of calls fail, open the circuit
                        .slidingWindowSize(10) // Consider last 10 calls for failure rate
                        .minimumNumberOfCalls(5) // Minimum calls before calculating failure rate
                        .permittedNumberOfCallsInHalfOpenState(3) // Calls allowed when half-open
                        .waitDurationInOpenState(Duration.ofSeconds(30)) // Time to wait before transitioning to half-open
                        .automaticTransitionFromOpenToHalfOpenEnabled(true)
                        .recordExceptions(Throwable.class) // Record all throwables
                );
    }

    /**
     * Provides default configurations for TimeLimiters.
     */
    @Bean
    public TimeLimiterConfigCustomizer defaultTimeLimiterConfigCustomizer() {
        return TimeLimiterConfigCustomizer
                .of("default", builder -> builder
                        .timeoutDuration(Duration.ofSeconds(5)) // Default timeout for an operation
                        .cancelRunningFuture(true)
                );
    }
    
    /**
     * Provides default configurations for Retries.
     */
    @Bean
    public RetryConfigCustomizer defaultRetryConfigCustomizer() {
        return RetryConfigCustomizer
                .of("default", builder -> builder
                        .maxAttempts(3) // Default number of retry attempts
                        .waitDuration(Duration.ofMillis(500)) // Default wait duration between retries
                        .retryOnResult(response -> false) // Example: Don't retry on specific results
                        .retryExceptions(java.io.IOException.class, java.util.concurrent.TimeoutException.class)
                        .ignoreExceptions(javax.management.ServiceNotFoundException.class) // Example
                        .failAfterMaxAttempts(true)
                );
    }

    // Example: Customizing a specific named circuit breaker instance if needed beyond YAML.
    /*
    @Bean
    public CircuitBreakerConfigCustomizer specificServiceCircuitBreakerConfig() {
        return CircuitBreakerConfigCustomizer
            .of("mySpecificServiceCB", builder -> builder
                .slidingWindowSize(20)
                .failureRateThreshold(60));
    }
    */

    // Bulkhead configuration is typically done in application.yml due to its instance-specific nature.
    // resilience4j.bulkhead:
    //   instances:
    //     myBackendBulkhead:
    //       maxConcurrentCalls: 10
    //       maxWaitDuration: 10ms
}