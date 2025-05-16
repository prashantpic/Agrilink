package com.thesss.platform.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Main Spring Boot application class that initializes and starts the Eureka Service Discovery server.
 * This class enables the Eureka server functionality and Spring Boot's auto-configuration,
 * which includes APM instrumentation via Micrometer for metrics and tracing as per REQ-17-005.
 */
@SpringBootApplication
@EnableEurekaServer
public class ServiceDiscoveryApplication {

    /**
     * The main method that serves as the entry point for the Spring Boot application.
     *
     * @param args command line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(ServiceDiscoveryApplication.class, args);
    }

}