package com.thesss.platform.farmer.domain.model;

import com.thesss.platform.farmer.domain.service.EncryptionService;
import lombok.Getter;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain entity representing farmer's bank account details.
 * REQ-FRM-011
 */
@Getter
public class BankAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id; // Internal identifier for the bank account entity
    private final String accountHolderName;
    private final String bankName;
    private final EncryptedValue accountNumber; // Encrypted
    private final String branchName;
    private final String ifscSwiftCode;
    private final String purposeOfBankDetails;

    // Private constructor, use static factory methods
    private BankAccount(UUID id, String accountHolderName, String bankName, EncryptedValue accountNumber,
                        String branchName, String ifscSwiftCode, String purposeOfBankDetails) {
        this.id = Objects.requireNonNull(id, "Bank account ID cannot be null.");
        if (accountHolderName == null || accountHolderName.isBlank()) {
            throw new IllegalArgumentException("Account holder name cannot be blank.");
        }
        if (bankName == null || bankName.isBlank()) {
            throw new IllegalArgumentException("Bank name cannot be blank.");
        }
        Objects.requireNonNull(accountNumber, "Encrypted account number cannot be null.");

        this.accountHolderName = accountHolderName;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.branchName = branchName;
        this.ifscSwiftCode = ifscSwiftCode;
        this.purposeOfBankDetails = purposeOfBankDetails;
    }

    public static BankAccount create(String accountHolderName, String bankName, String accountNumberPlaintext,
                                     String branchName, String ifscSwiftCode, String purposeOfBankDetails,
                                     EncryptionService encryptionService) {
        Objects.requireNonNull(encryptionService, "EncryptionService cannot be null for creating BankAccount.");
        if (accountNumberPlaintext == null || accountNumberPlaintext.isBlank()) {
            throw new IllegalArgumentException("Account number cannot be blank.");
        }
        EncryptedValue encryptedAccountNumber = encryptionService.encrypt(accountNumberPlaintext);
        return new BankAccount(UUID.randomUUID(), accountHolderName, bankName, encryptedAccountNumber,
                               branchName, ifscSwiftCode, purposeOfBankDetails);
    }

    public static BankAccount fromEncrypted(UUID id, String accountHolderName, String bankName, EncryptedValue encryptedAccountNumber,
                                            String branchName, String ifscSwiftCode, String purposeOfBankDetails) {
         return new BankAccount(id, accountHolderName, bankName, encryptedAccountNumber,
                               branchName, ifscSwiftCode, purposeOfBankDetails);
    }


    // Example business method if needed
    public BankAccount updatePurpose(String newPurpose) {
        return new BankAccount(this.id, this.accountHolderName, this.bankName, this.accountNumber,
                               this.branchName, this.ifscSwiftCode, newPurpose);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankAccount that = (BankAccount) o;
        return Objects.equals(id, that.id); // Entity equality based on ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BankAccount{" +
               "id=" + id +
               ", accountHolderName='" + accountHolderName + '\'' +
               ", bankName='" + bankName + '\'' +
               // DO NOT print accountNumber value
               ", accountNumber=" + (accountNumber != null ? "PRESENT" : "NOT_PRESENT") +
               ", branchName='" + branchName + '\'' +
               ", ifscSwiftCode='" + ifscSwiftCode + '\'' +
               ", purposeOfBankDetails='" + purposeOfBankDetails + '\'' +
               '}';
    }
}