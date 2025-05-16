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
public class FarmerRegistrationApiRequest {

    @NotBlank(message = "First name is mandatory")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Middle name cannot exceed 50 characters")
    private String middleName;

    @NotBlank(message = "Last name is mandatory")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @NotNull(message = "Date of birth is mandatory")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Gender is mandatory")
    // Assuming gender will be validated against a master data list in service layer
    // e.g., MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    private String gender; // REQ-FRM-003, REQ-FRM-005

    @NotBlank(message = "Primary phone number is mandatory")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid primary phone number format") // E.164 format example
    @Size(max = 20)
    private String primaryPhoneNumber; // REQ-FRM-006

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid secondary phone number format")
    @Size(max = 20)
    private String secondaryPhoneNumber; // REQ-FRM-007

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String emailAddress; // REQ-FRM-007

    @NotNull(message = "Address is mandatory")
    @Valid
    private AddressApiDto address; // REQ-FRM-008

    @Valid
    private CoordinatesApiDto homesteadCoordinates; // REQ-FRM-009

    @NotNull(message = "National identification is mandatory")
    @Valid
    private NationalIdApiDto nationalIdentification; // REQ-FRM-010

    @Valid
    private List<BankAccountApiDto> bankAccounts; // REQ-FRM-011

    @Min(value = 0, message = "Family size cannot be negative")
    private Integer familySize; // REQ-FRM-004

    @Min(value = 0, message = "Years of farming experience cannot be negative")
    private Integer yearsOfFarmingExperience; // REQ-FRM-004

    // Assuming education level will be validated against a master data list
    private String educationLevel; // REQ-FRM-004

    @NotBlank(message = "Preferred language is mandatory")
    // Assuming preferred language will be validated against a master data list
    private String preferredLanguage; // REQ-FRM-012

    @Valid
    private List<MembershipApiDto> memberships; // REQ-FRM-013, REQ-FRM-014

    @NotNull(message = "Consent details are mandatory")
    @Size(min = 1, message = "At least one consent detail is required")
    @Valid
    private List<ConsentDetailApiDto> consents; // REQ-FRM-021


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressApiDto {
        @NotBlank(message = "Street/Village is mandatory")
        @Size(max = 200)
        private String streetAddressVillage;

        @NotBlank(message = "Tehsil/Taluk/Block is mandatory")
        @Size(max = 100)
        private String tehsilTalukBlock;

        @NotBlank(message = "District is mandatory")
        @Size(max = 100)
        private String district;

        @NotBlank(message = "State/Province is mandatory")
        @Size(max = 100)
        private String stateProvince;

        @NotBlank(message = "Postal code is mandatory")
        @Size(max = 20)
        private String postalCode;

        @NotBlank(message = "Country is mandatory")
        @Size(max = 100)
        private String country;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinatesApiDto {
        @NotNull(message = "Latitude is mandatory")
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        private BigDecimal latitude;

        @NotNull(message = "Longitude is mandatory")
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        private BigDecimal longitude;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NationalIdApiDto {
        @NotBlank(message = "National ID type is mandatory")
        @Size(max = 50)
        private String nationalIdType;

        @NotBlank(message = "ID number is mandatory")
        @Size(max = 50) // Max size of plaintext before encryption
        private String idNumber;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankAccountApiDto {
        @NotBlank(message = "Account holder name is mandatory")
        @Size(max = 150)
        private String accountHolderName;

        @NotBlank(message = "Bank name is mandatory")
        @Size(max = 100)
        private String bankName;

        @NotBlank(message = "Account number is mandatory")
        @Size(max = 50) // Max size of plaintext before encryption
        private String accountNumber;

        @Size(max = 100)
        private String branchName;

        @Size(max = 20)
        private String ifscSwiftCode;
        
        @NotBlank(message = "Purpose of bank details is mandatory")
        @Size(max = 255)
        private String purposeOfBankDetails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembershipApiDto {
        @NotBlank(message = "Organization name is mandatory")
        @Size(max = 150)
        private String organizationName;

        @Size(max = 50)
        private String membershipId;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsentDetailApiDto {
        @NotNull(message = "Consent given flag is mandatory")
        private Boolean consentGiven;

        @NotBlank(message = "Consent purpose is mandatory")
        @Size(max = 255)
        private String consentPurpose;

        @NotBlank(message = "Consent version ID is mandatory")
        @Size(max = 50)
        private String consentVersionId;
    }
}