package com.thesss.platform.aggregators.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;

import java.time.Duration;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.thesss.platform.aggregators.projection.repository")
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    @Value("${spring.r2dbc.url}")
    private String r2dbcUrl; // Expected format: r2dbc:postgresql://host:port/database

    @Value("${spring.r2dbc.username}")
    private String username;

    @Value("${spring.r2dbc.password}")
    private String password;

    @Value("${spring.r2dbc.pool.max-size:10}")
    private int maxSize;

    @Value("${spring.r2dbc.pool.initial-size:1}")
    private int initialSize;

    @Value("${spring.r2dbc.pool.max-idle-time:30m}")
    private Duration maxIdleTime;

    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        // Parse R2DBC URL to extract host, port, database
        // This is a simplified parsing. A more robust parser might be needed.
        String url = r2dbcUrl.substring("r2dbc:postgresql://".length());
        String[] parts = url.split("/");
        String[] hostPort = parts[0].split(":");
        String host = hostPort[0];
        int port = Integer.parseInt(hostPort[1]);
        String database = parts[1];

        PostgresqlConnectionConfiguration config = PostgresqlConnectionConfiguration.builder()
                .host(host)
                .port(port)
                .database(database)
                .username(username)
                .password(password)
                .build();

        ConnectionPoolConfiguration poolConfig = ConnectionPoolConfiguration.builder(new PostgresqlConnectionFactory(config))
                .maxSize(maxSize)
                .initialSize(initialSize)
                .maxIdleTime(maxIdleTime)
                .build();

        return new ConnectionPool(poolConfig);
    }

    // Flyway or Liquibase configuration can be added here if needed.
    // For Spring Boot, if Flyway is on the classpath and enabled (spring.flyway.enabled=true),
    // it will automatically run migrations from src/main/resources/db/migration.
    // Similar for Liquibase.
}