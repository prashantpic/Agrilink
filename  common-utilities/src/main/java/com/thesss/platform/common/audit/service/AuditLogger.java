package com.thesss.platform.common.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thesss.platform.common.audit.model.AuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// The @Service annotation is suitable if this class has dependencies managed by Spring,
// but since it's instantiated directly in CommonAuditAutoConfiguration,
// it's not strictly necessary here if no other Spring features like AOP are applied to it directly.
// However, it's conventional for service-like beans.
// @Service
public class AuditLogger {

    // Use a specific logger name for audit events, configurable in logback.xml
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT");
    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class); // For internal logging

    private final ObjectMapper objectMapper;

    public AuditLogger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Logs an audit event in a structured format (JSON).
     *
     * @param event The AuditEvent object to log.
     */
    public void logEvent(AuditEvent event) {
        if (event == null) {
            log.warn("Attempted to log a null audit event.");
            return;
        }
        try {
            // Serialize AuditEvent to JSON
            String eventJson = objectMapper.writeValueAsString(event);
            // Log the JSON string using the dedicated audit logger
            AUDIT_LOGGER.info(eventJson);
        } catch (JsonProcessingException e) {
            // Log an error if serialization fails, include basic event info as fallback
            log.error("Failed to serialize audit event to JSON. Event ID: {}, Action: {}, Subject: {}. Error: {}",
                      event.getEventId(),
                      event.getAction() != null ? event.getAction().getName() : "N/A",
                      event.getSubject() != null ? event.getSubject().getId() : "N/A",
                      e.getMessage(), e);
            // As a fallback, might log essential details in a non-JSON format if JSON fails.
            // AUDIT_LOGGER.error("CRITICAL: Audit event serialization failed. Event: " + event.toString());
        }
    }
}