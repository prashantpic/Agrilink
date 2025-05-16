package com.thesss.platform.crop.config;

import com.thesss.platform.crop.infrastructure.clients.FarmerServiceClient;
import com.thesss.platform.crop.infrastructure.clients.LandServiceClient;
import com.thesss.platform.crop.infrastructure.clients.MasterDataServiceClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
// import feign.Logger;
// import feign.RequestInterceptor;
// import feign.codec.ErrorDecoder;
// import org.springframework.context.annotation.Bean;

@Configuration
@EnableFeignClients(basePackageClasses = {
    FarmerServiceClient.class,
    LandServiceClient.class,
    MasterDataServiceClient.class
    // Add other Feign client interfaces here if they are in different packages
})
public class RestClientConfig {

    // Default Feign configurations can be set in application.yml (e.g., connectTimeout, readTimeout).
    // Specific configurations per client can also be defined there or using a custom
    // Feign client configuration class referenced in the @FeignClient annotation.

    // Example of a global custom Feign configuration (applied to all clients if not overridden):
    /*
    @Bean
    public ErrorDecoder feignErrorDecoder() {
        return new CustomFeignErrorDecoder(); // Your custom error decoder implementation
    }

    @Bean
    public RequestInterceptor oauth2FeignRequestInterceptor() {
        // Example: Interceptor to add OAuth2 token for inter-service communication
        // This would typically use Spring Security's OAuth2AuthorizedClientService
        return requestTemplate -> {
            // String token = ... ; // Fetch token
            // requestTemplate.header("Authorization", "Bearer " + token);
        };
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // Set default logging level for Feign clients (can be overridden per client)
    }
    */

    // If specific configurations are needed for individual Feign clients,
    // they can be defined in separate @Configuration classes and referenced
    // in the @FeignClient(configuration = SpecificClientConfig.class) annotation.
}