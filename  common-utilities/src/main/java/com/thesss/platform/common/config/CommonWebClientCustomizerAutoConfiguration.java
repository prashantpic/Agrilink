package com.thesss.platform.common.config;

import com.thesss.platform.common.logging.interceptor.CorrelationIdClientHttpRequestInterceptor;
import com.thesss.platform.common.logging.interceptor.CorrelationIdExchangeFilterFunction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CommonWebClientCustomizerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.web.client.RestTemplate")
    public CorrelationIdClientHttpRequestInterceptor correlationIdClientHttpRequestInterceptor() {
        return new CorrelationIdClientHttpRequestInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = "org.springframework.web.reactive.function.client.WebClient")
    public CorrelationIdExchangeFilterFunction correlationIdExchangeFilterFunction() {
        return new CorrelationIdExchangeFilterFunction();
    }

    @Bean
    @ConditionalOnBean(CorrelationIdClientHttpRequestInterceptor.class)
    @Order(Ordered.HIGHEST_PRECEDENCE) // Ensure this customizer runs early
    public RestTemplateCustomizer customRestTemplateCustomizer(
            CorrelationIdClientHttpRequestInterceptor interceptor) {
        return restTemplate -> {
            // Add interceptor at the beginning to ensure it runs before others
            // that might depend on the correlation ID header.
            // Check if already present to avoid duplicates if customizer is called multiple times.
            if (restTemplate.getInterceptors().stream().noneMatch(i -> i.getClass().equals(CorrelationIdClientHttpRequestInterceptor.class))) {
                restTemplate.getInterceptors().add(0, interceptor);
            }
        };
    }

    @Bean
    @ConditionalOnBean(CorrelationIdExchangeFilterFunction.class)
    @Order(Ordered.HIGHEST_PRECEDENCE) // Ensure this customizer runs early
    public WebClientCustomizer customWebClientCustomizer(
            CorrelationIdExchangeFilterFunction filterFunction) {
        return webClientBuilder -> webClientBuilder.filter(filterFunction);
    }
}