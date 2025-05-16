package com.thesss.platform.farmer.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerSummaryApiResponse {

    private UUID farmerId;
    private String fullName; // Concatenated name
    private String primaryPhoneNumber;
    private String status;
    private LocalDateTime dateOfRegistration; // Optional, but often useful in summaries
}