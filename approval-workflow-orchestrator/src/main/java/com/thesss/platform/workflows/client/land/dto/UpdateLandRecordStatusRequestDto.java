package com.thesss.platform.workflows.client.land.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLandRecordStatusRequestDto {
    private String status; // e.g., "PENDING_VERIFICATION", "VERIFIED", "REJECTED"
    private String reasonForStatusChange;
}