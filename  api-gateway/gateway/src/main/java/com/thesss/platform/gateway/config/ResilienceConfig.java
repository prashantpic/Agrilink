package com.thesss.platform.gateway.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.client.circuitbreaker.Customizer;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    private static final Logger log = LoggerFactory.getLogger(ResilienceConfig.class);

    /**
     * Example of a programmatic customizer for the ReactiveResilience4JCircuitBreakerFactory.
     * This can be used to set default configurations for all circuit breakers
     * or configure specific ones if not fully managed by application.yml.
     * Property-based configuration in application.yml is generally preferred for simplicity.
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCircuitBreakerCustomizer() {
        return factory -> {
            factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                    .circuitBreakerConfig(CircuitBreakerConfig.custom()
                            .slidingWindowSize(10)
                            .failureRateThreshold(50.0f)
                            .waitDurationInOpenState(Duration.ofSeconds(10))
                            .permittedNumberOfCallsInHalfOpenState(3)
                            .slowCallRateThreshold(50.0f)
                            .slowCallDurationThreshold(Duration.ofSeconds(2))
                            .build())
                    .timeLimiterConfig(TimeLimiterConfig.custom()
                            .timeoutDuration(Duration.ofSeconds(5))
                            .build())
                    .build());

            // Example of configuring a specific circuit breaker instance
            // factory.configure(builder -> builder.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
            // .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4)).build()), "mySpecificCircuitBreaker");
        };
    }

    /**
     * Example of customizing the RetryRegistry.
     * This can be used to define default retry configurations.
     */
    @Bean
    public Customizer<RetryRegistry> defaultRetryRegistryCustomizer() {
        return registry -> {
            RetryConfig defaultRetryConfig = RetryConfig.custom()
                    .maxAttempts(3)
                    .waitDuration(Duration.ofMillis(500))
                    // Add more specific configurations if needed
                    .build();
            registry.addConfiguration("defaultRetryConfig", defaultRetryConfig);

            // You can also add event consumers for logging or monitoring retries
            registry.getEventPublisher().onEntryAdded(event -> log.info("Retry config added: {}", event.getAddedEntry().getName()));
        };
    }


    @Bean
    public RegistryEventConsumer<CircuitBreaker> circuitBreakerEventConsumer() {
        return new RegistryEventConsumer<>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
                log.info("CircuitBreaker registry: entry added {}", entryAddedEvent.getAddedEntry().getName());
                entryAddedEvent.getAddedEntry().getEventPublisher()
                    .onStateTransition(event ->
                        log.warn("CircuitBreaker {} state changed from {} to {}",
                                entryAddedEvent.getAddedEntry().getName(),
                                event.getStateTransition().getFromState(),
                                event.getStateTransition().getToState())
                    );
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {
                log.info("CircuitBreaker registry: entry removed {}", entryRemoveEvent.getRemovedEntry().getName());
            }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {
                log.info("CircuitBreaker registry: entry replaced old:{}, new:{}",
                    entryReplacedEvent.getOldEntry().getName(), entryReplacedEvent.getNewEntry().getName());
            }
        };
    }
}