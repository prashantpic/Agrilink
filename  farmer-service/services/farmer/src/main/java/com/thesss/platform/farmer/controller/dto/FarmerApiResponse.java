package com.thesss.platform.farmer.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FarmerApiResponse {

    private UUID farmerId; // REQ-FRM-002
    private String firstName;
    private String middleName;
    private String lastName;
    private String concatenatedFullName;
    private LocalDate dateOfBirth; // REQ-FRM-003
    private String gender; // REQ-FRM-003, REQ-FRM-005

    private String primaryPhoneNumber; // REQ-FRM-006
    private String secondaryPhoneNumber; // REQ-FRM-007
    private String emailAddress; // REQ-FRM-007

    private AddressApiDto address; // REQ-FRM-008
    private CoordinatesApiDto homesteadCoordinates; // REQ-FRM-009

    private NationalIdApiResponseDto nationalIdentification; // REQ-FRM-010 (masked)
    private List<BankAccountApiResponseDto> bankAccounts; // REQ-FRM-011 (masked)

    private Integer familySize; // REQ-FRM-004
    private Integer yearsOfFarmingExperience; // REQ-FRM-004
    private String educationLevel; // REQ-FRM-004
    private String preferredLanguage; // REQ-FRM-012

    private List<MembershipApiDto> memberships; // REQ-FRM-013, REQ-FRM-014

    private String status; // REQ-FRM-018
    private LocalDateTime dateOfStatusChange; // REQ-FRM-018
    private String reasonForStatusChange; // REQ-FRM-019

    private String profilePhotoUrl; // REQ-FRM-020

    private List<ConsentDetailApiResponseDto> consents; // REQ-FRM-021
    // Approval history is typically fetched via a separate endpoint, but can be included if needed.
    // private List<ApprovalHistoryApiResponse> approvalHistory; // REQ-FRM-022

    private AuditInfoApiDto auditInfo; // REQ-FRM-016, REQ-FRM-017

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressApiDto {
        private String streetAddressVillage;
        private String tehsilTalukBlock;
        private String district;
        private String stateProvince;
        private String postalCode;
        private String country;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CoordinatesApiDto {
        private BigDecimal latitude;
        private BigDecimal longitude;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NationalIdApiResponseDto {
        private String nationalIdType;
        private String maskedIdNumber; // e.g., "********1234" or just type if fully hidden
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BankAccountApiResponseDto {
        private String accountHolderName;
        private String bankName;
        private String maskedAccountNumber; // e.g., "********5678"
        private String branchName;
        private String ifscSwiftCode;
        private String purposeOfBankDetails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MembershipApiDto {
        private String organizationName;
        private String membershipId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConsentDetailApiResponseDto {
        private Boolean consentGiven;
        private LocalDateTime consentTimestamp;
        private String consentPurpose;
        private String consentVersionId;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuditInfoApiDto {
        private LocalDateTime dateOfRegistration;
        private String registeredBy;
        private LocalDateTime lastUpdatedDate;
        private String lastUpdatedBy;
    }
}