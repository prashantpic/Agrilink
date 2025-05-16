package com.thesss.platform.workflows.config;

import com.thesss.platform.workflows.exception.WorkflowException;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.cloud.openfeign.EnableFeignClients;

@Configuration
@EnableFeignClients(basePackages = "com.thesss.platform.workflows.client")
public class FeignClientConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeignClientConfig.class);

    @Bean
    public ErrorDecoder customErrorDecoder() {
        return (methodKey, response) -> {
            String errorMessage = String.format("Error calling %s. Status: %s, Reason: %s",
                    methodKey, response.status(), response.reason());
            LOGGER.error(errorMessage);
            // You can customize exception handling based on response status or body
            if (response.status() >= 400 && response.status() <= 499) {
                return new WorkflowException("Client error calling " + methodKey + ": " + response.reason());
            }
            if (response.status() >= 500 && response.status() <= 599) {
                return new WorkflowException("Server error calling " + methodKey + ": " + response.reason());
            }
            // Fallback to default error decoder
            return new ErrorDecoder.Default().decode(methodKey, response);
        };
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
                    String tokenValue = jwtAuthenticationToken.getToken().getTokenValue();
                    template.header("Authorization", "Bearer " + tokenValue);
                    LOGGER.debug("Propagating JWT token to Feign client request for: {}", template.feignTarget().name());
                } else {
                    LOGGER.warn("No JWT token found in SecurityContext. Feign request to {} will not be authenticated with JWT.", template.feignTarget().name());
                }
            }
        };
    }
    
    @Bean
    public Retryer feignRetryer() {
        // Default Retryer: 100ms period, 1s max period, 5 attempts
        return new Retryer.Default();
    }
}