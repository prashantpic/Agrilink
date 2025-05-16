package com.thesss.platform.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main Spring Boot application class for the API Gateway.
 * This class initializes and runs the API Gateway, enabling auto-configuration,
 * component scanning, and service discovery registration (e.g., with Eureka).
 *
 * ImplementedFeatures: ApplicationBootstrap, ServiceDiscoveryRegistration
 */
@SpringBootApplication
@EnableDiscoveryClient // Enables service registration and discovery with systems like Eureka, Consul, etc.
public class GatewayApplication {

    /**
     * Main method to launch the Spring Boot application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}