package com.thesss.platform.farmer.service.dto;

import com.thesss.platform.farmer.domain.model.Gender; // Assuming Gender enum exists
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Fields are optional for partial updates.
// Similar to FarmerCreateDto, using NationalIdDetailsDto and BankAccountDetailsDto.
// Other complex objects are represented by placeholder DTOs or flattened fields.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FarmerUpdateDto {

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

    private NationalIdDetailsDto nationalIdentification; // Plaintext ID number inside if being updated
    private List<BankAccountDetailsDto> bankAccounts;   // Plaintext account numbers inside if being updated/added

    private Integer familySize;
    private Integer yearsOfFarmingExperience;
    private String educationLevel;
    private String preferredLanguage;

    private List<FarmerCreateDto.MembershipDto> memberships; // Reusing from FarmerCreateDto for simplicity

    private List<FarmerCreateDto.ConsentDto> consents; // Reusing from FarmerCreateDto for simplicity
}