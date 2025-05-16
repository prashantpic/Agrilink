package com.thesss.platform.farmer.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountDetailsDto {
    private String accountHolderName;
    private String bankName;
    // This accountNumber could be plaintext if coming from a request to be encrypted,
    // or an encrypted string if representing data from the DB (via EncryptedValue.getValue()).
    // The service layer and mapper need to handle this context.
    private String accountNumber;
    private String branchName;
    private String ifscSwiftCode;
    private String purposeOfBankDetails;
}