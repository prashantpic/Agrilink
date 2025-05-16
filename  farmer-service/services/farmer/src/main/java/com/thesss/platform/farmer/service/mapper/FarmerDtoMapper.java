package com.thesss.platform.farmer.service.mapper;

import com.thesss.platform.farmer.controller.dto.*;
import com.thesss.platform.farmer.domain.model.*; // Assuming full domain model exists
import com.thesss.platform.farmer.service.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// Assuming domain objects (Farmer, FullName, Address, Coordinates, EncryptedValue, etc.)
// and other DTOs (FarmerRegistrationApiRequest, FarmerUpdateApiRequest, etc.) exist or will be generated.
// This mapper will have compile errors until all referenced types are available.

@Mapper(componentModel = "spring", uses = { /* EncryptionService.class if needed for direct en/de-cryption here */ })
public interface FarmerDtoMapper {

    // API Request to Service DTO Mappings
    FarmerCreateDto toFarmerCreateDto(FarmerRegistrationApiRequest request);
    FarmerUpdateDto toFarmerUpdateDto(FarmerUpdateApiRequest request);

    // Domain to Service DTO Mappings
    @Mapping(source = "id", target = "id", qualifiedByName = "farmerIdToUuid")
    @Mapping(source = "fullName.firstName", target = "firstName")
    @Mapping(source = "fullName.middleName", target = "middleName")
    @Mapping(source = "fullName.lastName", target = "lastName")
    @Mapping(source = "contactInformation.primaryPhoneNumber", target = "primaryPhoneNumber")
    @Mapping(source = "contactInformation.secondaryPhoneNumber", target = "secondaryPhoneNumber")
    @Mapping(source = "contactInformation.emailAddress", target = "emailAddress")
    @Mapping(source = "address.streetAddressVillage", target = "streetAddressVillage")
    @Mapping(source = "address.tehsilTalukBlock", target = "tehsilTalukBlock")
    @Mapping(source = "address.district", target = "district")
    @Mapping(source = "address.stateProvince", target = "stateProvince")
    @Mapping(source = "address.postalCode", target = "postalCode")
    @Mapping(source = "address.country", target = "country")
    @Mapping(source = "homesteadCoordinates.latitude", target = "latitude")
    @Mapping(source = "homesteadCoordinates.longitude", target = "longitude")
    @Mapping(source = "nationalIdentification", target = "nationalIdentification", qualifiedByName = "domainNationalIdToDto")
    @Mapping(source = "bankAccounts", target = "bankAccounts", qualifiedByName = "domainBankAccountsToDto")
    @Mapping(source = "memberships", target = "memberships") // Requires Membership -> MembershipDto mapping
    @Mapping(source = "consents", target = "consents")       // Requires Consent -> ConsentDto mapping
    @Mapping(source = "auditInfo.dateOfRegistration", target = "dateOfRegistration")
    @Mapping(source = "auditInfo.registeredBy", target = "registeredBy")
    @Mapping(source = "auditInfo.lastUpdatedDate", target = "lastUpdatedDate")
    @Mapping(source = "auditInfo.lastUpdatedBy", target = "lastUpdatedBy")
    @Mapping(source = "approvalHistory", target = "approvalHistory") // Requires ApprovalHistoryEntry -> ApprovalHistoryEntryDto mapping
    FarmerDetailsDto toFarmerDetailsDto(Farmer farmer);

    List<FarmerDetailsDto.ApprovalHistoryEntryDto> domainApprovalHistoryToDto(List<ApprovalHistoryEntry> historyEntries);
    FarmerDetailsDto.ApprovalHistoryEntryDto domainApprovalEntryToDto(ApprovalHistoryEntry entry);


    // Service DTO to API Response Mappings
    @Mapping(target = "nationalIdentification", expression = "java(mapNationalIdForApi(detailsDto.getNationalIdentification()))")
    @Mapping(target = "bankAccounts", expression = "java(mapBankAccountsForApi(detailsDto.getBankAccounts()))")
    FarmerApiResponse toFarmerApiResponse(FarmerDetailsDto detailsDto);

    @Mapping(source = "id", target = "farmerId", qualifiedByName = "farmerIdToUuid")
    @Mapping(source = "fullName", target = "fullName", qualifiedByName = "fullNameToString")
    @Mapping(source = "contactInformation.primaryPhoneNumber", target = "primaryPhoneNumber")
    FarmerSummaryApiResponse toFarmerSummaryApiResponse(Farmer farmer);

    // Helper for FarmerSummaryApiResponse if mapping from FarmerDetailsDto
    @Mapping(target = "fullName", expression = "java(detailsDto.getFirstName() + (detailsDto.getMiddleName() != null ? \" \" + detailsDto.getMiddleName() : \"\") + \" \" + detailsDto.getLastName())")
    FarmerSummaryApiResponse toFarmerSummaryApiResponse(FarmerDetailsDto detailsDto);


    // --- Potentially needed mappings for nested objects if not using @Mapping annotations for all fields ---
    // Example: FarmerCreateDto parts to Domain VOs (often handled by Factory)
    // FullName fromFarmerCreateDtoToFullName(FarmerCreateDto dto);
    // Address fromFarmerCreateDtoToAddress(FarmerCreateDto dto);
    // Coordinates fromFarmerCreateDtoToCoordinates(FarmerCreateDto dto); // BigDecimal lat/lon to Coordinates VO


    // --- Qualified By Name methods for complex mappings ---
    @Named("farmerIdToUuid")
    default UUID farmerIdToUuid(FarmerId farmerId) {
        return farmerId == null ? null : farmerId.getValue();
    }

    @Named("uuidToFarmerId")
    default FarmerId uuidToFarmerId(UUID uuid) {
        return uuid == null ? null : FarmerId.of(uuid);
    }
    
    @Named("fullNameToString")
    default String fullNameToString(FullName fullName) {
        if (fullName == null) return null;
        StringBuilder sb = new StringBuilder();
        sb.append(fullName.getFirstName());
        if (fullName.getMiddleName() != null && !fullName.getMiddleName().isEmpty()) {
            sb.append(" ").append(fullName.getMiddleName());
        }
        sb.append(" ").append(fullName.getLastName());
        return sb.toString();
    }

    @Named("domainNationalIdToDto")
    default NationalIdDetailsDto domainNationalIdToDto(NationalIdentification domainNi) {
        if (domainNi == null) return null;
        return NationalIdDetailsDto.builder()
                .nationalIdType(domainNi.getNationalIdType())
                .idNumber(domainNi.getIdNumber().getValue()) // Exposes encrypted string
                .build();
    }

    @Named("domainBankAccountsToDto")
    default List<BankAccountDetailsDto> domainBankAccountsToDto(List<BankAccount> domainBankAccounts) {
        if (domainBankAccounts == null) return null;
        return domainBankAccounts.stream().map(this::domainBankAccountToDto).toList();
    }

    default BankAccountDetailsDto domainBankAccountToDto(BankAccount domainBa) {
        if (domainBa == null) return null;
        return BankAccountDetailsDto.builder()
                .accountHolderName(domainBa.getAccountHolderName())
                .bankName(domainBa.getBankName())
                .accountNumber(domainBa.getAccountNumber().getValue()) // Exposes encrypted string
                .branchName(domainBa.getBranchName())
                .ifscSwiftCode(domainBa.getIfscSwiftCode())
                .purposeOfBankDetails(domainBa.getPurposeOfBankDetails())
                .build();
    }

    // Mapping for API Response (masking)
    default FarmerApiResponse.NationalIdentifierApiDto mapNationalIdForApi(NationalIdDetailsDto niDetailsDto) {
        if (niDetailsDto == null || niDetailsDto.getNationalIdType() == null) {
            return null;
        }
        // Example: Only show type, mask number for API response
        return new FarmerApiResponse.NationalIdentifierApiDto(niDetailsDto.getNationalIdType(), "****");
    }

    default List<FarmerApiResponse.BankAccountApiDto> mapBankAccountsForApi(List<BankAccountDetailsDto> bankAccountDetailsDtos) {
        if (bankAccountDetailsDtos == null) {
            return null;
        }
        return bankAccountDetailsDtos.stream()
                .map(dto -> new FarmerApiResponse.BankAccountApiDto(
                        dto.getAccountHolderName(),
                        dto.getBankName(),
                        maskAccountNumber(dto.getAccountNumber()), // Masked
                        dto.getBranchName(),
                        dto.getIfscSwiftCode(),
                        dto.getPurposeOfBankDetails()))
                .toList();
    }

    default String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return "****";
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }

    // Mapping API DTOs (ConsentApiRequest, ApprovalHistoryEntryDto) to API Response DTOs
    // These are placeholders as the source DTOs are not fully defined/generated yet
    ApprovalHistoryApiResponse toApprovalHistoryApiResponse(FarmerDetailsDto.ApprovalHistoryEntryDto dto);

    // Page mapping
    default Page<FarmerSummaryApiResponse> toFarmerSummaryApiResponsePage(Page<Farmer> page) {
        return page.map(this::toFarmerSummaryApiResponse);
    }

     // Placeholder for MembershipDto mapping
    List<FarmerCreateDto.MembershipDto> domainMembershipsToDto(List<Membership> memberships);
    FarmerCreateDto.MembershipDto domainMembershipToDto(Membership membership);

    // Placeholder for ConsentDto mapping
    List<FarmerCreateDto.ConsentDto> domainConsentsToDto(List<Consent> consents);
    FarmerCreateDto.ConsentDto domainConsentToDto(Consent consent);


    // The following mappings assume simplified DTOs for FarmerRegistrationApiRequest etc.
    // Actual mappings will depend on the final structure of these API request DTOs.

    @Mapping(target = "nationalIdentification", source="nationalIdentification")
    @Mapping(target = "bankAccounts", source="bankAccounts")
    @Mapping(target = "memberships", source="memberships")
    @Mapping(target = "consents", source="consents")
    FarmerCreateDto.MembershipDto mapApiMembershipToServiceDto(FarmerRegistrationApiRequest.MembershipApiDto apiDto);
    FarmerCreateDto.ConsentDto mapApiConsentToServiceDto(FarmerRegistrationApiRequest.ConsentApiDto apiDto);


    // Mapping from service DTO (e.g. FarmerCreateDto.CoordinatesDto if it existed) to Domain Coordinates
    // Typically done in FarmerFactory, but if mapper is used:
    // @Mapping(source = "latitude", target = "latitude")
    // @Mapping(source = "longitude", target = "longitude")
    // Coordinates toDomainCoordinates(FarmerCreateDto.CoordinatesDto dto);
}