package com.thesss.platform.farmer.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApprovalHistoryApiResponse {

    private String fieldNameChanged;
    private String previousValue; // Should be masked if sensitive
    private String newValue; // Should be masked if sensitive
    private String submittedByUserId;
    private LocalDateTime submissionDate;
    private String approvalStatus; // e.g., PENDING, APPROVED, REJECTED, BYPASSED_ADMIN
    private String approvedRejectedByUserId;
    private LocalDateTime approvalRejectionDate;
    private String approverComments;
}