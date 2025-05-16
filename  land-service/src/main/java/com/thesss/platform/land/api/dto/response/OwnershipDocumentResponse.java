package com.thesss.platform.land.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data // REQ-2-008
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OwnershipDocumentResponse {
    private UUID id;
    private String documentUrl;
    private LocalDate expiryDate;
}