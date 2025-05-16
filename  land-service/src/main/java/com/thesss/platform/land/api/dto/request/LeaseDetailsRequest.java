package com.thesss.platform.land.api.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data // REQ-2-009
@Builder
public class LeaseDetailsRequest {

    // Optional ID for updates, not present for creation
    private UUID id;

    @NotBlank(message = "Lessor name is required.")
    @Size(max = 100, message = "Lessor name must not exceed 100 characters.")
    private String lessorName;

    @NotBlank(message = "Lessor contact is required.")
    @Size(max = 100, message = "Lessor contact must not exceed 100 characters.")
    private String lessorContact;

    @PastOrPresent(message = "Lease start date must be in the past or present.")
    private LocalDate leaseStartDate;

    @FutureOrPresent(message = "Lease end date must be in the future or present.")
    private LocalDate leaseEndDate;

    @Size(max = 1000, message = "Lease terms must not exceed 1000 characters.")
    private String leaseTerms;

    // Custom validation for leaseStartDate <= leaseEndDate can be added using a class-level validator if needed
}