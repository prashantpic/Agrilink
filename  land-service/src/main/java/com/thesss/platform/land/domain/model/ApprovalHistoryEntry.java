package com.thesss.platform.land.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing an entry in the approval history for a farm land record.
 * REQ-2-021
 */
@Getter
@Setter(AccessLevel.PRIVATE) // Internal state changes controlled by methods
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For JPA/MapStruct
public class ApprovalHistoryEntry {

    private UUID id;
    private String fieldChanged; // e.g., "StatusUpdate", "BoundaryChange", "ParcelIdUpdate"
    private String previousValue; // Optional, string representation
    private String newValue; // Optional, string representation
    private String status; // Status of THIS approval entry (e.g., "LOGGED", "PENDING_WORKFLOW", "APPROVED_BY_WORKFLOW")
    private String submittedBy; // User or system component
    private Instant submissionDate;
    private String approvedBy; // User or system component that approved (if applicable)
    private Instant approvalDate; // Timestamp of approval (if applicable)
    private String comments; // Comments related to this change or approval
    // AuditInfo for the history entry itself might be overkill unless history entries are audited.

    public ApprovalHistoryEntry(UUID id, String fieldChanged, String previousValue, String newValue,
                                String status, String submittedBy, Instant submissionDate,
                                String approvedBy, Instant approvalDate, String comments) {
        this.id = Objects.requireNonNull(id, "ApprovalHistoryEntry ID cannot be null.");
        this.fieldChanged = Objects.requireNonNull(fieldChanged, "FieldChanged cannot be null.");
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.status = Objects.requireNonNull(status, "Approval entry status cannot be null.");
        this.submittedBy = Objects.requireNonNull(submittedBy, "SubmittedBy cannot be null.");
        this.submissionDate = Objects.requireNonNull(submissionDate, "SubmissionDate cannot be null.");
        this.approvedBy = approvedBy;
        this.approvalDate = approvalDate;
        this.comments = comments;
    }

    // No public update methods usually; approval history entries are typically immutable once created.
    // Status of an approval request might be updated by an external workflow.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApprovalHistoryEntry that = (ApprovalHistoryEntry) o;
        return Objects.equals(id, that.id); // Entity equality based on ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id); // Entity hashCode based on ID
    }
}