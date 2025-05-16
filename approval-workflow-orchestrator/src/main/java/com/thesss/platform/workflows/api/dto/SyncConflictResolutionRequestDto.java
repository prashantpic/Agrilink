package com.thesss.platform.workflows.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncConflictResolutionRequestDto {

    @NotBlank(message = "Conflict ID cannot be blank")
    private String conflictId;

    @NotBlank(message = "Entity type cannot be blank")
    private String entityType;

    @NotBlank(message = "Entity ID cannot be blank")
    private String entityId;

    @NotBlank(message = "Reported by User ID cannot be blank")
    private String reportedByUserId;

    @NotBlank(message = "Offline version details cannot be blank")
    private String offlineVersionDetails; // Typically a JSON string of the offline entity

    @NotBlank(message = "Server version details cannot be blank")
    private String serverVersionDetails;  // Typically a JSON string of the server entity
}