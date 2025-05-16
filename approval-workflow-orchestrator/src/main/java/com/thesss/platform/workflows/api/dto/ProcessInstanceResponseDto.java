package com.thesss.platform.workflows.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessInstanceResponseDto {
    private String processInstanceId;
    private String businessKey;
    private String status; // e.g., "STARTED", "PENDING_APPROVAL"
}