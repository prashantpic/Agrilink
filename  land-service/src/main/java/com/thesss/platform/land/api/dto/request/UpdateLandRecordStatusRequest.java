package com.thesss.platform.land.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data // REQ-2-019
@Builder
public class UpdateLandRecordStatusRequest {

    @NotBlank(message = "Status is required.")
    @Size(max = 50, message = "Status code must not exceed 50 characters.")
    // Assuming status is passed as a String code that maps to LandRecordStatus enum or Master Data
    private String status;

    @Size(max = 255, message = "Reason for status change must not exceed 255 characters.")
    private String reason; // Optional depending on the status transition
}