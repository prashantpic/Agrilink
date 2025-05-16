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
public class ConsentApiRequest {

    @NotNull(message = "Consent given flag is mandatory")
    private Boolean consentGiven;

    @NotBlank(message = "Consent purpose is mandatory")
    @Size(max = 255, message = "Consent purpose cannot exceed 255 characters")
    private String consentPurpose;

    @NotBlank(message = "Consent version ID is mandatory")
    @Size(max = 50, message = "Consent version ID cannot exceed 50 characters")
    private String consentVersionId;
}