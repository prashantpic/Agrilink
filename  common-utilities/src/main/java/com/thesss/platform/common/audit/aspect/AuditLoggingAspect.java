package com.thesss.platform.common.audit.aspect;

import com.thesss.platform.common.audit.annotation.Auditable;
import com.thesss.platform.common.audit.extractor.AuditDataExtractor;
import com.thesss.platform.common.audit.model.AuditAction;
import com.thesss.platform.common.audit.model.AuditEvent;
import com.thesss.platform.common.audit.model.AuditResource;
import com.thesss.platform.common.audit.model.AuditSubject;
import com.thesss.platform.common.audit.service.AuditLogger;
import com.thesss.platform.common.logging.util.MDCContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Aspect
public class AuditLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLoggingAspect.class);

    private final AuditLogger auditLogger;
    private final AuditDataExtractor auditDataExtractor;

    public AuditLoggingAspect(AuditLogger auditLogger, AuditDataExtractor auditDataExtractor) {
        this.auditLogger = auditLogger;
        this.auditDataExtractor = auditDataExtractor;
    }

    @Around("@annotation(auditableAnnotation) || @within(auditableAnnotation)")
    public Object logAuditAround(ProceedingJoinPoint joinPoint, Auditable auditableAnnotation) throws Throwable {
        // The 'auditableAnnotation' parameter will be the most specific one (method if present, else class).

        Instant startTime = Instant.now();
        AuditEvent auditEvent = new AuditEvent();
        auditEvent.setEventId(UUID.randomUUID().toString());
        auditEvent.setTimestamp(startTime);

        Object result = null;
        Throwable exception = null;

        try {
            // 1. Extract pre-execution data
            AuditSubject subject = auditDataExtractor.extractSubject(joinPoint, auditableAnnotation);
            auditEvent.setSubject(subject);

            AuditAction action = auditDataExtractor.extractAction(joinPoint, auditableAnnotation);
            auditEvent.setAction(action);

            auditEvent.setClientIpAddress(auditDataExtractor.extractClientIpAddress(joinPoint));
            auditEvent.setCorrelationId(MDCContext.getCorrelationId());

            // 2. Proceed with method execution
            result = joinPoint.proceed();
            auditEvent.setStatus("SUCCESS");
            return result;
        } catch (Throwable e) {
            exception = e;
            auditEvent.setStatus("FAILURE");
            // Log details about the failure if needed, e.g., exception type and message
            Map<String, Object> additionalDetails = new HashMap<>();
            additionalDetails.put("errorType", e.getClass().getName());
            additionalDetails.put("errorMessage", e.getMessage());
            auditEvent.setAdditionalDetails(additionalDetails);
            throw e; // Re-throw to allow normal exception handling flow
        } finally {
            // 3. Extract post-execution data (resource state)
            AuditResource resource = auditDataExtractor.extractResource(joinPoint, auditableAnnotation, result, exception);
            auditEvent.setResource(resource); // Sets Optional<AuditResource>

            // Log the complete audit event
            auditLogger.logEvent(auditEvent);
            if (log.isDebugEnabled()) {
                 MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                 log.debug("Audit event logged for method: {}.{} with status: {}, action: {}",
                         signature.getDeclaringTypeName(), signature.getName(),
                         auditEvent.getStatus(), auditEvent.getAction().getName());
            }
        }
    }
}