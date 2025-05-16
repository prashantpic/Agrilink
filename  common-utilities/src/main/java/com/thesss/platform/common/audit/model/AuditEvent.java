package com.thesss.platform.common.audit.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditEvent {

    private String eventId; // UUID

    @JsonSerialize(using = InstantSerializer.class)
    private Instant timestamp;

    private AuditSubject subject;
    private AuditAction action;
    private AuditResource resource; // Optional, but including for consistency
    private String status; // e.g., SUCCESS, FAILURE
    private String clientIpAddress; // Optional
    private String correlationId; // Optional
    private Map<String, Object> additionalDetails; // Optional

    public AuditEvent() {
    }

    public AuditEvent(String eventId, Instant timestamp, AuditSubject subject, AuditAction action, AuditResource resource, String status, String clientIpAddress, String correlationId, Map<String, Object> additionalDetails) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.subject = subject;
        this.action = action;
        this.resource = resource; // Can be null if not applicable
        this.status = status;
        this.clientIpAddress = clientIpAddress;
        this.correlationId = correlationId;
        this.additionalDetails = additionalDetails;
    }

    // Getters and Setters

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public AuditSubject getSubject() {
        return subject;
    }

    public void setSubject(AuditSubject subject) {
        this.subject = subject;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public AuditResource getResource() {
        return resource;
    }

    public void setResource(AuditResource resource) {
        this.resource = resource;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClientIpAddress() {
        return clientIpAddress;
    }

    public void setClientIpAddress(String clientIpAddress) {
        this.clientIpAddress = clientIpAddress;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Map<String, Object> getAdditionalDetails() {
        return additionalDetails;
    }

    public void setAdditionalDetails(Map<String, Object> additionalDetails) {
        this.additionalDetails = additionalDetails;
    }

    @Override
    public String toString() {
        return "AuditEvent{" +
                "eventId='" + eventId + '\'' +
                ", timestamp=" + timestamp +
                ", subject=" + subject +
                ", action=" + action +
                ", resource=" + resource +
                ", status='" + status + '\'' +
                ", clientIpAddress='" + clientIpAddress + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", additionalDetails=" + additionalDetails +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditEvent that = (AuditEvent) o;
        return Objects.equals(eventId, that.eventId) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(subject, that.subject) &&
                Objects.equals(action, that.action) &&
                Objects.equals(resource, that.resource) &&
                Objects.equals(status, that.status) &&
                Objects.equals(clientIpAddress, that.clientIpAddress) &&
                Objects.equals(correlationId, that.correlationId) &&
                Objects.equals(additionalDetails, that.additionalDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, timestamp, subject, action, resource, status, clientIpAddress, correlationId, additionalDetails);
    }
}