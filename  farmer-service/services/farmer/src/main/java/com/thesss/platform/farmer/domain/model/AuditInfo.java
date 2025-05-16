package com.thesss.platform.farmer.domain.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Embeddable value object for audit timestamps and user tracking.
 * REQ-FRM-016, REQ-FRM-017
 */
public final class AuditInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalDateTime dateOfRegistration;
    private final String registeredBy;
    private final LocalDateTime lastUpdatedDate;
    private final String lastUpdatedBy;

    public AuditInfo(LocalDateTime dateOfRegistration, String registeredBy,
                     LocalDateTime lastUpdatedDate, String lastUpdatedBy) {
        this.dateOfRegistration = Objects.requireNonNull(dateOfRegistration, "Date of registration cannot be null.");
        this.registeredBy = Objects.requireNonNull(registeredBy, "Registered by user cannot be null.");
        this.lastUpdatedDate = Objects.requireNonNull(lastUpdatedDate, "Last updated date cannot be null.");
        this.lastUpdatedBy = Objects.requireNonNull(lastUpdatedBy, "Last updated by user cannot be null.");
    }
    
    public static AuditInfo initial(String registeredBy) {
        LocalDateTime now = LocalDateTime.now();
        return new AuditInfo(now, registeredBy, now, registeredBy);
    }

    public AuditInfo recordUpdate(String updatedBy) {
        return new AuditInfo(this.dateOfRegistration, this.registeredBy, LocalDateTime.now(), updatedBy);
    }


    public LocalDateTime dateOfRegistration() {
        return dateOfRegistration;
    }

    public String registeredBy() {
        return registeredBy;
    }

    public LocalDateTime lastUpdatedDate() {
        return lastUpdatedDate;
    }

    public String lastUpdatedBy() {
        return lastUpdatedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditInfo auditInfo = (AuditInfo) o;
        return Objects.equals(dateOfRegistration, auditInfo.dateOfRegistration) &&
               Objects.equals(registeredBy, auditInfo.registeredBy) &&
               Objects.equals(lastUpdatedDate, auditInfo.lastUpdatedDate) &&
               Objects.equals(lastUpdatedBy, auditInfo.lastUpdatedBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateOfRegistration, registeredBy, lastUpdatedDate, lastUpdatedBy);
    }

    @Override
    public String toString() {
        return "AuditInfo{" +
               "dateOfRegistration=" + dateOfRegistration +
               ", registeredBy='" + registeredBy + '\'' +
               ", lastUpdatedDate=" + lastUpdatedDate +
               ", lastUpdatedBy='" + lastUpdatedBy + '\'' +
               '}';
    }
}