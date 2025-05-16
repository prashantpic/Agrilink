package com.thesss.platform.workflows.client.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalHistoryEntryDto {
    // Fields for specific change being approved/audited
    private String fieldName; // Optional, if logging history for a specific field change
    private String previousValue; // Optional
    private String newValue; // Optional

    // Submission context
    private String submittedByUserId;
    private OffsetDateTime submissionDate;

    // Approval/Rejection context
    private String approvalStatus; // e.g., "SUBMITTED_FOR_APPROVAL", "APPROVED", "REJECTED"
    private String approvedRejectedByUserId; // User who approved/rejected
    private OffsetDateTime approvalRejectionDate;
    private String approverComments;

    // Workflow context
    private String workflowInstanceId; // ID of the Camunda process instance that handled this
}