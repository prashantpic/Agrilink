package com.thesss.platform.land.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data // REQ-2-021
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApprovalHistoryEntryResponse {
    private UUID id;
    private String fieldChanged;
    private String previousValue;
    private String newValue;
    private String status;
    private String submittedBy;
    private Instant submissionDate;
    private String approvedBy;
    private Instant approvalDate;
    private String comments;
}