package com.thesss.platform.farmer.domain.model;

import lombok.Getter;
import lombok.Setter; // Should be used sparingly, prefer immutable updates or specific methods

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain aggregate root representing a farmer.
 * Encapsulates all data and business logic related to a farmer's profile.
 * REQ-FRM-001 to REQ-FRM-024
 */
@Getter
public class Farmer {

    private FarmerId farmerId;
    private FullName fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private ContactInformation contactInformation;
    private Address address;
    private Coordinates homesteadCoordinates;
    private NationalIdentification nationalIdentification; // Contains EncryptedValue

    @Setter // Example of a mutable list, manage through methods
    private List<BankAccount> bankAccounts = new ArrayList<>();

    private Integer familySize;
    private Integer yearsOfFarmingExperience;
    private String educationLevel; // Consider a VO or link to Master Data
    private String preferredLanguage; // Consider a VO or link to Master Data

    @Setter // Example of a mutable list, manage through methods
    private List<Membership> memberships = new ArrayList<>();

    private FarmerStatus status;
    private LocalDateTime dateOfStatusChange;
    private String reasonForStatusChange;

    @Setter
    private String profilePhotoUrl;

    @Setter // Example of a mutable list, manage through methods
    private List<Consent> consents = new ArrayList<>();

    @Setter // Example of a mutable list, manage through methods
    private List<ApprovalHistoryEntry> approvalHistory = new ArrayList<>();

    private AuditInfo auditInfo;

    // Private constructor, creation is handled by FarmerFactory
    // This constructor is for the factory's use.
    Farmer(FarmerId farmerId, FullName fullName, LocalDate dateOfBirth, Gender gender,
           ContactInformation contactInformation, Address address, Coordinates homesteadCoordinates,
           NationalIdentification nationalIdentification, Integer familySize, Integer yearsOfFarmingExperience,
           String educationLevel, String preferredLanguage, FarmerStatus initialStatus, AuditInfo auditInfo) {
        this.farmerId = Objects.requireNonNull(farmerId, "FarmerId cannot be null");
        this.fullName = Objects.requireNonNull(fullName, "FullName cannot be null");
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth, "Date of birth cannot be null");
        this.gender = Objects.requireNonNull(gender, "Gender cannot be null");
        this.contactInformation = Objects.requireNonNull(contactInformation, "ContactInformation cannot be null");
        this.address = address; // Address can be optional depending on requirements
        this.homesteadCoordinates = homesteadCoordinates; // Coordinates can be optional
        this.nationalIdentification = nationalIdentification; // National ID can be optional
        this.familySize = familySize;
        this.yearsOfFarmingExperience = yearsOfFarmingExperience;
        this.educationLevel = educationLevel;
        this.preferredLanguage = preferredLanguage;
        this.status = Objects.requireNonNull(initialStatus, "Initial status cannot be null");
        this.dateOfStatusChange = LocalDateTime.now(); // Or from auditInfo registration date
        this.auditInfo = Objects.requireNonNull(auditInfo, "AuditInfo cannot be null");

        // Initialize lists
        this.bankAccounts = new ArrayList<>();
        this.memberships = new ArrayList<>();
        this.consents = new ArrayList<>();
        this.approvalHistory = new ArrayList<>();
    }

    // Business Methods
    public void updateFullName(FullName newFullName, String updatedBy) {
        Objects.requireNonNull(newFullName, "New full name cannot be null.");
        this.fullName = newFullName;
        this.updateAuditInfo(updatedBy);
    }

    public void updateContactInformation(ContactInformation newContactInformation, String updatedBy) {
        Objects.requireNonNull(newContactInformation, "New contact information cannot be null.");
        // Add validation if primary phone number uniqueness is violated (though primarily app service resp)
        this.contactInformation = newContactInformation;
        this.updateAuditInfo(updatedBy);
    }

    public void updateAddress(Address newAddress, String updatedBy) {
        this.address = newAddress; // Assuming address can be nullable or fully replaced
        this.updateAuditInfo(updatedBy);
    }
    
    public void updateHomesteadCoordinates(Coordinates newCoordinates, String updatedBy) {
        this.homesteadCoordinates = newCoordinates;
        this.updateAuditInfo(updatedBy);
    }

    public void updateNationalIdentification(NationalIdentification newNationalIdentification, String updatedBy) {
        this.nationalIdentification = newNationalIdentification;
        this.updateAuditInfo(updatedBy);
    }

    public void updatePersonalDetails(LocalDate newDateOfBirth, Gender newGender, Integer newFamilySize,
                                      Integer newYearsOfExperience, String newEducationLevel,
                                      String newPreferredLanguage, String updatedBy) {
        if (newDateOfBirth != null) this.dateOfBirth = newDateOfBirth;
        if (newGender != null) this.gender = newGender;
        if (newFamilySize != null) this.familySize = newFamilySize;
        if (newYearsOfExperience != null) this.yearsOfFarmingExperience = newYearsOfExperience;
        if (newEducationLevel != null) this.educationLevel = newEducationLevel;
        if (newPreferredLanguage != null) this.preferredLanguage = newPreferredLanguage;
        this.updateAuditInfo(updatedBy);
    }


    public void changeStatus(FarmerStatus newStatus, String reason, String updatedBy) {
        Objects.requireNonNull(newStatus, "New status cannot be null.");
        if (this.status == newStatus) {
            return; // No change
        }
        this.status = newStatus;
        this.reasonForStatusChange = reason;
        this.dateOfStatusChange = LocalDateTime.now();
        this.updateAuditInfo(updatedBy);
    }

    public void recordConsent(Consent consentEntry, String updatedBy) {
        Objects.requireNonNull(consentEntry, "Consent entry cannot be null.");
        // Logic to update existing consent or add new
        // For simplicity, adding it to list. Real app might replace based on purpose/version.
        this.consents.removeIf(c -> c.getConsentPurpose().equals(consentEntry.getConsentPurpose()) && c.getConsentVersionId().equals(consentEntry.getConsentVersionId()));
        this.consents.add(consentEntry);
        this.updateAuditInfo(updatedBy);
    }

    public void addApprovalEntry(ApprovalHistoryEntry entry, String updatedBy) {
        Objects.requireNonNull(entry, "Approval history entry cannot be null.");
        this.approvalHistory.add(entry);
        // No audit info update here as this is part of an operation already audited
    }

    public void addBankAccount(BankAccount bankAccount, String updatedBy) {
        Objects.requireNonNull(bankAccount, "Bank account cannot be null.");
        this.bankAccounts.add(bankAccount);
        this.updateAuditInfo(updatedBy);
    }

    public void removeBankAccount(UUID bankAccountId, String updatedBy) {
        this.bankAccounts.removeIf(ba -> ba.getId().equals(bankAccountId));
        this.updateAuditInfo(updatedBy);
    }
    
    public void updateBankAccount(BankAccount updatedBankAccount, String updatedBy) {
        Objects.requireNonNull(updatedBankAccount, "Updated bank account cannot be null");
        this.bankAccounts.removeIf(ba -> ba.getId().equals(updatedBankAccount.getId()));
        this.bankAccounts.add(updatedBankAccount);
        this.updateAuditInfo(updatedBy);
    }

    public void addMembership(Membership membership, String updatedBy) {
        Objects.requireNonNull(membership, "Membership cannot be null.");
        this.memberships.add(membership);
        this.updateAuditInfo(updatedBy);
    }

    public void removeMembership(Membership membership, String updatedBy) {
         // Equality for VOs should work here
        this.memberships.remove(membership);
        this.updateAuditInfo(updatedBy);
    }
    
    private void updateAuditInfo(String updatedBy) {
        this.auditInfo = new AuditInfo(
                this.auditInfo.dateOfRegistration(),
                this.auditInfo.registeredBy(),
                LocalDateTime.now(),
                updatedBy
        );
    }

    // Override equals and hashCode based on FarmerId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Farmer farmer = (Farmer) o;
        return Objects.equals(farmerId, farmer.farmerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(farmerId);
    }
}