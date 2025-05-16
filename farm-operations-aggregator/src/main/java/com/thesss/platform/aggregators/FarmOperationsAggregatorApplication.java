package com.thesss.platform.aggregators;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableR2dbcRepositories // Explicitly enable R2DBC repositories, though often auto-configured
public class FarmOperationsAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FarmOperationsAggregatorApplication.class, args);
    }

}