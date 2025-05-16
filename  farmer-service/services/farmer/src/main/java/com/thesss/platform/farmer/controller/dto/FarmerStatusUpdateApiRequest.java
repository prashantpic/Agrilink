package com.thesss.platform.farmer.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FarmerStatusUpdateApiRequest {

    @NotBlank(message = "New status is mandatory")
    // Assuming status will be validated against allowed FarmerStatus enum values in service layer
    // e.g., ACTIVE, INACTIVE, SUSPENDED, DECEASED
    private String newStatus;

    @Size(max = 500, message = "Reason for status change cannot exceed 500 characters")
    private String reasonForStatusChange;
}