package com.thesss.platform.land.api.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data // REQ-2-008
@Builder
public class OwnershipDocumentRequest {

    // Optional ID for updates, not present for creation
    private UUID id;

    @NotBlank(message = "Document URL is required.")
    @Size(max = 2048, message = "Document URL must not exceed 2048 characters.")
    // @Pattern(regexp = "URL_REGEX_HERE", message = "Invalid URL format for document.") // Add URL pattern if needed
    private String documentUrl; // URL reference to object storage

    @FutureOrPresent(message = "Document expiry date must be in the future or present, if provided.")
    private LocalDate expiryDate; // Optional
}