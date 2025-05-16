package com.thesss.platform.workflows.api.dto;

import com.thesss.platform.workflows.model.process.FieldChangeData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriticalDataApprovalRequestDto {

    @NotBlank(message = "Entity type cannot be blank")
    private String entityType; // e.g., "Farmer", "LandPlot", "BankAccount"

    @NotBlank(message = "Entity ID cannot be blank")
    private String entityId;

    @NotBlank(message = "Submitted by User ID cannot be blank")
    private String submittedByUserId;

    @NotEmpty(message = "Changed fields cannot be empty")
    @Valid
    private List<FieldChangeData> changedFields;
}