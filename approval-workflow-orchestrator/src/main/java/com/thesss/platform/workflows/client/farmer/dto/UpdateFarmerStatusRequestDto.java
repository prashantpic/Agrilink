package com.thesss.platform.workflows.client.farmer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFarmerStatusRequestDto {
    private String status; // e.g., "PENDING_APPROVAL", "ACTIVE", "REJECTED", "SUSPENDED"
    private String reasonForStatusChange;
}