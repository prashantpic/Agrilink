package com.thesss.platform.crop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
// import org.springframework.context.annotation.Bean;
// import org.springframework.data.domain.AuditorAware;
// import java.util.Optional;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;


@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    // If @CreatedBy or @LastModifiedBy are used in entities,
    // an AuditorAware bean needs to be provided.
    // Example:
    /*
    @Bean
    public AuditorAware<String> auditorProvider() {
        // Implement logic to return current user's principal name or ID.
        // This typically involves Spring Security.
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                             .filter(Authentication::isAuthenticated)
                             .map(Authentication::getName);
    }
    */
}