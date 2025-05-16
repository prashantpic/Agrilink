package com.thesss.platform.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MeterRegistryCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = "io.micrometer.core.instrument.MeterRegistry")
public class CommonMetricsAutoConfiguration {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTagsMeterRegistryCustomizer(
            @Value("${spring.application.name:unknown-service}") String applicationName) {
        return registry -> registry.config().commonTags("application", applicationName);
    }
}