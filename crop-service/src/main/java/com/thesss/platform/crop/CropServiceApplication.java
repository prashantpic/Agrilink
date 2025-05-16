package com.thesss.platform.crop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // Enables JPA auditing features like @CreatedDate and @LastModifiedDate
@EnableFeignClients(basePackages = "com.thesss.platform.crop.infrastructure.clients") // Enable Feign clients in the infrastructure layer
public class CropServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CropServiceApplication.class, args);
    }

}