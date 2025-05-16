package com.thesss.platform.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thesss.platform.common.audit.aspect.AuditLoggingAspect;
import com.thesss.platform.common.audit.extractor.AuditDataExtractor;
import com.thesss.platform.common.audit.extractor.DefaultAuditDataExtractor;
import com.thesss.platform.common.audit.service.AuditLogger;
// import com.thesss.platform.common.security.util.SecurityUtils; // Not needed as direct param for DefaultAuditDataExtractor bean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy // Enables AOP proxying for @Aspect classes
@ConditionalOnProperty(name = "common.audit.enabled", havingValue = "true", matchIfMissing = true)
public class CommonAuditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuditLogger auditLogger(ObjectMapper objectMapper) {
        // ObjectMapper is provided by Spring Boot's default auto-configuration
        return new AuditLogger(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditDataExtractor defaultAuditDataExtractor() {
        // DefaultAuditDataExtractor will use SecurityUtils.staticMethods internally
        return new DefaultAuditDataExtractor();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditLoggingAspect auditLoggingAspect(AuditLogger auditLogger, AuditDataExtractor auditDataExtractor) {
        return new AuditLoggingAspect(auditLogger, auditDataExtractor);
    }
}