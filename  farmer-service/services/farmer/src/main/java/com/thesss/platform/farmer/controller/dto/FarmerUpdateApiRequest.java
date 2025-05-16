package com.thesss.platform.farmer.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FarmerUpdateApiRequest {

    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Middle name cannot exceed 50 characters")
    private String middleName;

    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    // Assuming gender will be validated against a master data list in service layer
    private String gender;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid primary phone number format")
    @Size(max = 20)
    private String primaryPhoneNumber;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid secondary phone number format")
    @Size(max = 20)
    private String secondaryPhoneNumber;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String emailAddress;

    @Valid
    private AddressApiDto address;

    @Valid
    private CoordinatesApiDto homesteadCoordinates;

    @Valid
    private NationalIdApiDto nationalIdentification;

    @Valid
    private List<BankAccountApiDto> bankAccounts;

    @Min(value = 0, message = "Family size cannot be negative")
    private Integer familySize;

    @Min(value = 0, message = "Years of farming experience cannot be negative")
    private Integer yearsOfFarmingExperience;

    private String educationLevel;

    // Assuming preferred language will be validated against a master data list
    private String preferredLanguage;

    @Valid
    private List<MembershipApiDto> memberships;

    // Consents are typically managed via a separate endpoint or method call for specific purposes,
    // but including here if a general update can modify consents.
    // It's often better to use a dedicated consent management endpoint.
    // For this DTO, it implies updating existing consent records if provided.
    @Valid
    private List<ConsentDetailApiDto> consents;

    // Re-using nested DTOs from FarmerRegistrationApiRequest for consistency.
    // Fields within these nested DTOs are also optional for update.

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressApiDto {
        @Size(max = 200)
        private String streetAddressVillage;
        @Size(max = 100)
        private String tehsilTalukBlock;
        @Size(max = 100)
        private String district;
        @Size(max = 100)
        private String stateProvince;
        @Size(max = 20)
        private String postalCode;
        @Size(max = 100)
        private String country;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinatesApiDto {
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        private BigDecimal latitude;

        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        private BigDecimal longitude;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NationalIdApiDto {
        @Size(max = 50)
        private String nationalIdType;
        @Size(max = 50)
        private String idNumber; // Plaintext if being updated
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankAccountApiDto {
        @Size(max = 150)
        private String accountHolderName;
        @Size(max = 100)
        private String bankName;
        @Size(max = 50)
        private String accountNumber; // Plaintext if being updated
        @Size(max = 100)
        private String branchName;
        @Size(max = 20)
        private String ifscSwiftCode;
        @Size(max = 255)
        private String purposeOfBankDetails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembershipApiDto {
        @Size(max = 150)
        private String organizationName;
        @Size(max = 50)
        private String membershipId;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsentDetailApiDto {
        private Boolean consentGiven;
        @Size(max = 255)
        private String consentPurpose;
        @Size(max = 50)
        private String consentVersionId;
    }
}