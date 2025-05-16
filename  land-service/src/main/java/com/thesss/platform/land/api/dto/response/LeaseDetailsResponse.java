package com.thesss.platform.land.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data // REQ-2-009
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeaseDetailsResponse {
    private UUID id;
    private String lessorName;
    private String lessorContact;
    private LocalDate leaseStartDate;
    private LocalDate leaseEndDate;
    private String leaseTerms;
}