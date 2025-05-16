package com.thesss.platform.workflows.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FarmerRegistrationApprovalRequestDto {

    @NotBlank(message = "Farmer ID cannot be blank")
    private String farmerId;

    @NotBlank(message = "Submitted by User ID cannot be blank")
    private String submittedByUserId;

    @NotEmpty(message = "Registration data cannot be empty")
    private Map<String, Object> registrationData; // Can contain various fields from the registration form
}