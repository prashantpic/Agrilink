package com.thesss.platform.farmer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableDiscoveryClient // To register with a discovery server like Eureka, Consul, etc.
@EnableJpaAuditing // Enables JPA Auditing, auditorProvider bean should be configured elsewhere (e.g. JpaAuditingConfig.java)
public class FarmerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FarmerServiceApplication.class, args);
    }

}