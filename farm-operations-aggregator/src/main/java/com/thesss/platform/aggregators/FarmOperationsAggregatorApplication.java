package com.thesss.platform.aggregators;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka // Enable Kafka listener support
@EnableR2dbcRepositories // Enable Spring Data R2DBC repositories
public class FarmOperationsAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FarmOperationsAggregatorApplication.class, args);
    }

}