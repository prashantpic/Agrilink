package com.thesss.platform.land.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing lease/rental details for a farm land record.
 * REQ-2-009
 */
@Getter
@Setter(AccessLevel.PRIVATE) // Internal state changes controlled by methods
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA/MapStruct
public class LeaseDetails {

    private UUID id;
    private String lessorName;
    private String lesseeName; // Farmer is typically the lessee
    private String contactInfo; // Lessor's contact
    private LocalDate startDate;
    private LocalDate endDate;
    private String terms;
    // AuditInfo might be relevant here too
    // private AuditInfo auditInfo;

    public LeaseDetails(UUID id, String lessorName, String lesseeName, String contactInfo,
                        LocalDate startDate, LocalDate endDate, String terms) {
        this.id = Objects.requireNonNull(id, "LeaseDetails ID cannot be null.");
        this.lessorName = lessorName;
        this.lesseeName = lesseeName;
        this.contactInfo = contactInfo;
        setDates(startDate, endDate); // Use method for validation
        this.terms = terms;
    }

    public void updateDetails(String lessorName, String lesseeName, String contactInfo,
                              LocalDate startDate, LocalDate endDate, String terms) {
        this.lessorName = lessorName;
        this.lesseeName = lesseeName;
        this.contactInfo = contactInfo;
        setDates(startDate, endDate);
        this.terms = terms;
        // Update audit info if applicable
    }

    private void setDates(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Lease end date cannot be before start date.");
        }
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaseDetails that = (LeaseDetails) o;
        return Objects.equals(id, that.id); // Entity equality based on ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Entity hashCode based on ID
    }
}