package com.thesss.platform.farmer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
// Assuming Spring Security is used or some other way to get the current user.
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        // Implement logic to return the current user's ID.
        // This is a placeholder. In a real application, this would come from
        // Spring Security context or a similar mechanism.
        // For example, if using Spring Security:
        // return () -> {
        //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //     if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
        //         return Optional.of("system"); // Or handle as an error/default
        //     }
        //     return Optional.of(authentication.getName());
        // };
        return () -> Optional.of("system-user"); // Placeholder
    }
}