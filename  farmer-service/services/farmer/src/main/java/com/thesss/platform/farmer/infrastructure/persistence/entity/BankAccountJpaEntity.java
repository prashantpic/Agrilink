package com.thesss.platform.farmer.infrastructure.persistence.entity;

import com.thesss.platform.farmer.infrastructure.persistence.converter.JpaEncryptionConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "farmer_bank_accounts")
public class BankAccountJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @GeneratedValue(strategy = GenerationType.AUTO) // Or another strategy
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    private FarmerJpaEntity farmer;

    @Column(nullable = false, length = 150)
    private String accountHolderName;

    @Column(nullable = false, length = 100)
    private String bankName;

    @Convert(converter = JpaEncryptionConverter.class)
    @Column(name = "encrypted_account_number", nullable = false, length = 255)
    private String encryptedAccountNumber; // This string IS the EncryptedValue.value

    @Column(length = 100)
    private String branchName;

    @Column(name = "ifsc_swift_code", length = 20)
    private String ifscSwiftCode;

    @Column(length = 255) // Purpose can be descriptive
    private String purposeOfBankDetails;
}