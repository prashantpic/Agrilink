package com.thesss.platform.aggregators.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.thesss.platform.aggregators.projection.repository")
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    @Value("${spring.r2dbc.username}")
    private String username;

    @Value("${spring.r2dbc.password}")
    private String password;

    @Value("${spring.r2dbc.url}")
    private String r2dbcUrl; // Expected format: r2dbc:postgresql://host:port/database

    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        // The r2dbcUrl format is "r2dbc:postgresql://<host>:<port>/<database>"
        // We need to parse host, port, database from this URL for PostgresqlConnectionConfiguration
        // Or, ensure properties for host, port, database are separate.
        // For simplicity, assuming r2dbcUrl is correctly formatted and PostgresqlConnectionFactory handles it.
        // If not, parse manually:
        // e.g., String url = r2dbcUrl.substring("r2dbc:postgresql://".length());
        // String[] parts = url.split(":");
        // String host = parts[0];
        // String[] portAndDb = parts[1].split("/");
        // int port = Integer.parseInt(portAndDb[0]);
        // String database = portAndDb[1];

        // A more robust way using PostgresqlConnectionConfiguration if you have separate properties:
        // return new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
        // .host(dbHost)
        // .port(dbPort)
        // .database(dbName)
        // .username(username)
        // .password(password)
        // .build());

        // If spring.r2dbc.url is directly usable by ConnectionFactories.get(url) which Spring Boot auto-config does.
        // When extending AbstractR2dbcConfiguration, we must provide the bean.
        // The `r2dbcUrl` should correctly point to the PostgreSQL instance.
        // Let's assume standard parsing, spring.r2dbc.url for host/port/db, and explicit user/pass.
        // Example: r2dbc:postgresql://localhost:5432/farm_ops_read_model

        String effectiveUrl = r2dbcUrl;
        if (!effectiveUrl.startsWith("r2dbc:")) { // Ensure it's a full R2DBC URL
            effectiveUrl = "r2dbc:" + effectiveUrl;
        }
        
        String dbHost = extractHostFromR2dbcUrl(effectiveUrl);
        int dbPort = extractPortFromR2dbcUrl(effectiveUrl);
        String dbName = extractDatabaseFromR2dbcUrl(effectiveUrl);

        return new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
                .host(dbHost)
                .port(dbPort)
                .database(dbName)
                .username(username)
                .password(password)
                .build());
    }

    // Helper methods to parse R2DBC URL (simplistic parsing)
    private String extractHostFromR2dbcUrl(String url) {
        // r2dbc:postgresql://host:port/database
        return url.split("://")[1].split(":")[0];
    }

    private int extractPortFromR2dbcUrl(String url) {
        return Integer.parseInt(url.split("://")[1].split(":")[1].split("/")[0]);
    }

    private String extractDatabaseFromR2dbcUrl(String url) {
        return url.split("://")[1].split("/")[1].split("\\?")[0]; // Remove query params if any
    }

    // Schema management (Flyway/Liquibase) is typically configured separately
    // or via Spring Boot auto-configuration if the dependencies are present.
    // For Flyway, add `spring.flyway.url`, `spring.flyway.user`, `spring.flyway.password`.
    // Spring Boot will auto-configure Flyway if `org.flywaydb:flyway-core` is on the classpath.
    // R2DBC doesn't directly integrate with Flyway/Liquibase in the same way JDBC does.
    // Schema migrations are usually run using their JDBC drivers before the R2DBC application starts,
    // or via a separate migration task.
    // The `src/main/resources/db/migration` path is a convention for Flyway.
}