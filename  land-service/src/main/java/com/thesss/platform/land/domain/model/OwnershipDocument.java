package com.thesss.platform.land.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing an ownership or lease document associated with a farm land record.
 * REQ-2-008
 */
@Getter
@Setter(AccessLevel.PRIVATE) // Internal state changes controlled by methods
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA/MapStruct
public class OwnershipDocument {

    private UUID id;
    private String documentUrl;
    private LocalDate expiryDate; // Optional
    // AuditInfo might be relevant here too if documents have their own audit trail
    // private AuditInfo auditInfo;


    public OwnershipDocument(UUID id, String documentUrl, LocalDate expiryDate) {
        this.id = Objects.requireNonNull(id, "Document ID cannot be null.");
        setDocumentUrl(documentUrl); // Use setter for validation
        this.expiryDate = expiryDate;
    }

    private void setDocumentUrl(String documentUrl) {
        if (documentUrl == null || documentUrl.isBlank()) {
            throw new IllegalArgumentException("Document URL cannot be null or blank.");
        }
        // Basic URL format validation could be added here, but extensive validation
        // (e.g., reachability) is an application/infrastructure concern.
        this.documentUrl = documentUrl;
    }

    public void updateDetails(String documentUrl, LocalDate expiryDate) {
        setDocumentUrl(documentUrl);
        this.expiryDate = expiryDate;
        // Update audit info if applicable
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OwnershipDocument that = (OwnershipDocument) o;
        return Objects.equals(id, that.id); // Entity equality based on ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Entity hashCode based on ID
    }
}