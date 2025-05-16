package com.thesss.platform.farmer.service.dto;

import com.thesss.platform.farmer.domain.model.FarmerStatus; // Assuming FarmerStatus enum exists
import com.thesss.platform.farmer.domain.model.Gender;     // Assuming Gender enum exists
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// This DTO represents full farmer details.
// Sensitive fields (national ID, bank account numbers) if present here might be
// EncryptedValue.getValue() (i.e., the encrypted string) or plaintext if decrypted by the service
// for internal use. The mapper to FarmerApiResponse will handle masking/omission.
// For simplicity, if NationalIdDetailsDto/BankAccountDetailsDto are used here, they might carry
// plaintext if service decrypted, or encrypted string if service passed it through.
// Let's assume NationalIdDetailsDto/BankAccountDetailsDto here will hold the *encrypted* string
// if they are to be mapped from EncryptedValue, or the service populates them with plaintext
// if it has decrypted them for some internal logic before this DTO is mapped to an API response.

// To align with the idea that sensitive data in *this DTO* could be EncryptedValue's string form:
// NationalIdDetailsDto and BankAccountDetailsDto will have their sensitive fields as String.
// These strings would be the Base64 encoded encrypted values.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerDetailsDto {

    private UUID id;

    // From FullName
    private String firstName;
    private String middleName;
    private String lastName;

    private LocalDate dateOfBirth;
    private Gender gender;

    // From ContactInformation
    private String primaryPhoneNumber;
    private String secondaryPhoneNumber;
    private String emailAddress;

    // From Address
    private String streetAddressVillage;
    private String tehsilTalukBlock;
    private String district;
    private String stateProvince;
    private String postalCode;
    private String country;

    // From Coordinates
    private BigDecimal latitude;
    private BigDecimal longitude;

    // NationalIdentification: idNumber here should be the encrypted string
    private NationalIdDetailsDto nationalIdentification;

    // BankAccounts: accountNumber here should be the encrypted string
    private List<BankAccountDetailsDto> bankAccounts;

    private Integer familySize;
    private Integer yearsOfFarmingExperience;
    private String educationLevel;
    private String preferredLanguage;

    private List<FarmerCreateDto.MembershipDto> memberships; // Reusing, assuming it holds relevant data

    private FarmerStatus status;
    private LocalDateTime dateOfStatusChange;
    private String reasonForStatusChange;
    private String profilePhotoUrl;

    private List<FarmerCreateDto.ConsentDto> consents; // Reusing, assuming it holds relevant data

    // AuditInfo fields
    private LocalDateTime dateOfRegistration;
    private String registeredBy;
    private LocalDateTime lastUpdatedDate;
    private String lastUpdatedBy;

    private List<ApprovalHistoryEntryDto> approvalHistory; // Assuming ApprovalHistoryEntryDto exists

    // Placeholder for ApprovalHistoryEntryDto if not generated in this turn
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApprovalHistoryEntryDto {
        private String fieldNameChanged;
        private String previousValue;
        private String newValue;
        private String submittedByUserId;
        private LocalDateTime submissionDate;
        private String approvalStatus;
        private String approvedRejectedByUserId;
        private LocalDateTime approvalRejectionDate;
        private String approverComments;
    }
}