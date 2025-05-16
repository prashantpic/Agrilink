package com.thesss.platform.farmer.domain.event;

import com.thesss.platform.farmer.domain.model.FarmerId;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Domain event published when a critical field change is submitted for approval.
 * REQ-FRM-023
 */
public record CriticalFieldChangeSubmittedForApprovalEvent(
    FarmerId farmerId,
    Map<String, Object> changedFields, // Field name to new value
    String submittedByUserId,
    LocalDateTime submissionTimestamp
) {
    public CriticalFieldChangeSubmittedForApprovalEvent {
        Objects.requireNonNull(farmerId, "FarmerId cannot be null");
        Objects.requireNonNull(changedFields, "Changed fields map cannot be null");
        Objects.requireNonNull(submittedByUserId, "SubmittedByUserId cannot be null");
        Objects.requireNonNull(submissionTimestamp, "SubmissionTimestamp cannot be null");
    }
}