package com.thesss.platform.farmer.domain.service;

import com.thesss.platform.farmer.domain.model.*;
import com.thesss.platform.farmer.service.dto.FarmerCreateDto; // Using service DTO for creation command
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Factory for creating Farmer aggregate instances.
 * REQ-FRM-001, REQ-FRM-002
 */
@Component
public class FarmerFactory {

    private final EncryptionService encryptionService;

    public FarmerFactory(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    public Farmer createFarmer(FarmerCreateDto createDto, String registeredByUserId) {
        Assert.notNull(createDto, "FarmerCreateDto cannot be null");
        Assert.hasText(registeredByUserId, "RegisteredByUserId cannot be blank");

        FarmerId farmerId = FarmerId.generate();

        FullName fullName = new FullName(
                createDto.getFirstName(),
                createDto.getMiddleName(),
                createDto.getLastName()
        );

        ContactInformation contactInformation = new ContactInformation(
                createDto.getPrimaryPhoneNumber(),
                createDto.getSecondaryPhoneNumber(),
                createDto.getEmailAddress()
        );

        Address address = (createDto.getAddress() != null) ? new Address(
                createDto.getAddress().getStreetAddressVillage(),
                createDto.getAddress().getTehsilTalukBlock(),
                createDto.getAddress().getDistrict(),
                createDto.getAddress().getStateProvince(),
                createDto.getAddress().getPostalCode(),
                createDto.getAddress().getCountry()
        ) : null;

        Coordinates coordinates = (createDto.getCoordinates() != null) ? new Coordinates(
                createDto.getCoordinates().getLatitude(),
                createDto.getCoordinates().getLongitude()
        ) : null;

        NationalIdentification nationalIdentification = (createDto.getNationalIdentification() != null) ?
                NationalIdentification.create(
                        createDto.getNationalIdentification().getNationalIdType(),
                        createDto.getNationalIdentification().getIdNumber(), // Plaintext from DTO
                        encryptionService
                ) : null;
        
        AuditInfo auditInfo = AuditInfo.initial(registeredByUserId);

        Farmer farmer = new Farmer(
                farmerId,
                fullName,
                createDto.getDateOfBirth(),
                Gender.fromString(createDto.getGender()), // Assuming DTO has gender as String
                contactInformation,
                address,
                coordinates,
                nationalIdentification,
                createDto.getFamilySize(),
                createDto.getYearsOfFarmingExperience(),
                createDto.getEducationLevel(),
                createDto.getPreferredLanguage(),
                FarmerStatus.PENDING_APPROVAL, // Default initial status
                auditInfo
        );

        // Bank Accounts
        if (createDto.getBankAccounts() != null) {
            createDto.getBankAccounts().forEach(baDto ->
                farmer.addBankAccount(BankAccount.create(
                        baDto.getAccountHolderName(),
                        baDto.getBankName(),
                        baDto.getAccountNumber(), // Plaintext from DTO
                        baDto.getBranchName(),
                        baDto.getIfscSwiftCode(),
                        baDto.getPurposeOfBankDetails(),
                        encryptionService
                ), registeredByUserId)
            );
        }

        // Memberships
        if (createDto.getMemberships() != null) {
            createDto.getMemberships().forEach(memDto ->
                farmer.addMembership(new Membership(
                        memDto.getOrganizationName(),
                        memDto.getMembershipId()
                ), registeredByUserId)
            );
        }
        
        // Consents
        if (createDto.getConsents() != null) {
            createDto.getConsents().forEach(conDto ->
                farmer.recordConsent(new Consent(
                        conDto.isConsentGiven(),
                        conDto.getConsentPurpose(),
                        conDto.getConsentVersionId()
                ), registeredByUserId)
            );
        } else { // Add a default consent if not provided, or handle as per specific req.
             // Example: Default consent for platform usage
            // farmer.recordConsent(new Consent(true, "Platform Data Usage", "1.0"), registeredByUserId);
        }


        return farmer;
    }
}