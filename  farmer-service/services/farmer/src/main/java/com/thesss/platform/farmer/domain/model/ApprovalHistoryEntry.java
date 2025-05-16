package com.thesss.platform.farmer.domain.model;

import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing an entry in the farmer's approval history.
 * REQ-FRM-022, REQ-FRM-024
 */
@Getter
public class ApprovalHistoryEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id; // Internal ID for the history entry
    private final String fieldNameChanged; // Optional
    private final String previousValue; // Optional
    private final String newValue;
    private final String submittedByUserId;
    private final LocalDateTime submissionDate;
    private final String approvalStatus; // e.g., PENDING, APPROVED, REJECTED, BYPASSED_ADMIN
    private final String approvedRejectedByUserId; // Optional
    private final LocalDateTime approvalRejectionDate; // Optional
    private final String approverComments; // Optional

    // Full constructor
    public ApprovalHistoryEntry(String fieldNameChanged, String previousValue, String newValue,
                                String submittedByUserId, LocalDateTime submissionDate, String approvalStatus,
                                String approvedRejectedByUserId, LocalDateTime approvalRejectionDate, String approverComments) {
        this.id = UUID.randomUUID();
        if (newValue == null) { // New value is generally required.
            // throw new IllegalArgumentException("New value cannot be null for an approval entry.");
        }
        if (submittedByUserId == null || submittedByUserId.isBlank()) {
            throw new IllegalArgumentException("Submitted by User ID cannot be blank.");
        }
        Objects.requireNonNull(submissionDate, "Submission date cannot be null.");
        if (approvalStatus == null || approvalStatus.isBlank()) {
            throw new IllegalArgumentException("Approval status cannot be blank.");
        }

        this.fieldNameChanged = fieldNameChanged;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.submittedByUserId = submittedByUserId;
        this.submissionDate = submissionDate;
        this.approvalStatus = approvalStatus;
        this.approvedRejectedByUserId = approvedRejectedByUserId;
        this.approvalRejectionDate = approvalRejectionDate;
        this.approverComments = approverComments;
    }
    
    // Constructor for loading from persistence
    public ApprovalHistoryEntry(UUID id, String fieldNameChanged, String previousValue, String newValue,
                                String submittedByUserId, LocalDateTime submissionDate, String approvalStatus,
                                String approvedRejectedByUserId, LocalDateTime approvalRejectionDate, String approverComments) {
        this.id = Objects.requireNonNull(id);
        this.fieldNameChanged = fieldNameChanged;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.submittedByUserId = Objects.requireNonNull(submittedByUserId);
        this.submissionDate = Objects.requireNonNull(submissionDate);
        this.approvalStatus = Objects.requireNonNull(approvalStatus);
        this.approvedRejectedByUserId = approvedRejectedByUserId;
        this.approvalRejectionDate = approvalRejectionDate;
        this.approverComments = approverComments;
    }


    // Simplified constructor for initial submission
    public static ApprovalHistoryEntry newSubmission(String fieldNameChanged, String previousValue, String newValue, String submittedByUserId) {
        return new ApprovalHistoryEntry(
                fieldNameChanged,
                previousValue,
                newValue,
                submittedByUserId,
                LocalDateTime.now(),
                "PENDING", // Default status on submission
                null,
                null,
                null
        );
    }

    // Method to update entry upon approval/rejection (immutable pattern - returns new instance)
    public ApprovalHistoryEntry recordApproval(String newStatus, String reviewerId, String comments) {
        if (!"APPROVED".equalsIgnoreCase(newStatus) && !"REJECTED".equalsIgnoreCase(newStatus)) {
            throw new IllegalArgumentException("Invalid approval status. Must be APPROVED or REJECTED.");
        }
        return new ApprovalHistoryEntry(
            this.id, // Keep the same ID
            this.fieldNameChanged, this.previousValue, this.newValue, this.submittedByUserId,
            this.submissionDate, newStatus, reviewerId, LocalDateTime.now(), comments
        );
    }
    
    public static ApprovalHistoryEntry newAdminBypassEntry(String fieldNameChanged, String previousValue, String newValue, String adminUserId, String comments) {
        return new ApprovalHistoryEntry(
                fieldNameChanged,
                previousValue,
                newValue,
                adminUserId, // Submitted by admin
                LocalDateTime.now(),
                "BYPASSED_ADMIN", // Special status
                adminUserId, // Approved by admin (implicitly)
                LocalDateTime.now(),
                comments
        );
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApprovalHistoryEntry that = (ApprovalHistoryEntry) o;
        return Objects.equals(id, that.id); // Entity equality by ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ApprovalHistoryEntry{" +
               "id=" + id +
               ", fieldNameChanged='" + fieldNameChanged + '\'' +
               ", approvalStatus='" + approvalStatus + '\'' +
               ", submittedByUserId='" + submittedByUserId + '\'' +
               ", submissionDate=" + submissionDate +
               '}';
    }
}