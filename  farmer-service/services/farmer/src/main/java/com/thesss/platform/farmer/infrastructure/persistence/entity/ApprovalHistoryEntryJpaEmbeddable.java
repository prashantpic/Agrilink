package com.thesss.platform.farmer.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class ApprovalHistoryEntryJpaEmbeddable {

    @Column(length = 100) // Name of the field that was changed
    private String fieldNameChanged;

    @Column(columnDefinition = "TEXT") // Previous value of the field
    private String previousValue;

    @Column(nullable = false, columnDefinition = "TEXT") // New value of the field
    private String newValue;

    @Column(nullable = false, length = 50) // User ID of who submitted the change
    private String submittedByUserId;

    @Column(nullable = false) // Timestamp of submission
    private LocalDateTime submissionDate;

    @Column(nullable = false, length = 30) // e.g., PENDING_APPROVAL, APPROVED, REJECTED, ADMIN_MODIFIED
    private String approvalStatus;

    @Column(length = 50) // User ID of who approved/rejected
    private String approvedRejectedByUserId;

    private LocalDateTime approvalRejectionDate; // Timestamp of approval/rejection

    @Column(columnDefinition = "TEXT")
    private String approverComments;
}