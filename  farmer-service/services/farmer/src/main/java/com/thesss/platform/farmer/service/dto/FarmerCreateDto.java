package com.thesss.platform.farmer.service.dto;

import com.thesss.platform.farmer.domain.model.Gender; // Assuming Gender enum exists
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Assuming nested DTOs like FullNameDto, ContactInformationDto, AddressDto, CoordinatesDto,
// MembershipDto, ConsentDto will be defined elsewhere or fields will be flattened.
// For now, using NationalIdDetailsDto and BankAccountDetailsDto as they are in the generation list.
// Other complex objects are represented by placeholder DTOs or flattened fields.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerCreateDto {

    // From FullName
    private String firstName;
    private String middleName;
    private String lastName;

    private LocalDate dateOfBirth;
    private Gender gender; // Assuming Gender is an enum

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

    private NationalIdDetailsDto nationalIdentification; // Plaintext ID number inside
    private List<BankAccountDetailsDto> bankAccounts;   // Plaintext account numbers inside

    private Integer familySize;
    private Integer yearsOfFarmingExperience;
    private String educationLevel; // Could be an ID to master data
    private String preferredLanguage; // Could be an ID to master data

    // From Membership (simplified)
    private List<MembershipDto> memberships; // Assuming MembershipDto exists

    // From Consent (simplified)
    private List<ConsentDto> consents; // Assuming ConsentDto exists


    // Placeholder DTOs for fields that would have their own DTOs
    // These would typically be separate files.
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MembershipDto {
        private String organizationName;
        private String membershipId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConsentDto {
        private boolean consentGiven;
        private String consentPurpose;
        private String consentVersionId;
    }
}