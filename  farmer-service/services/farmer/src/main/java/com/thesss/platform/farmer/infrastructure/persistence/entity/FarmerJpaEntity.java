package com.thesss.platform.farmer.infrastructure.persistence.entity;

import com.thesss.platform.farmer.domain.model.FarmerStatus; // Assuming domain model exists
import com.thesss.platform.farmer.domain.model.Gender; // Assuming domain model exists
import com.thesss.platform.farmer.infrastructure.persistence.converter.JpaEncryptionConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "farmers")
public class FarmerJpaEntity {

    @Id
    @JdbcTypeCode(SqlTypes.VARCHAR) // Explicitly set for UUIDs if not using native UUID type in DB
    private UUID id; // Maps to FarmerId.value

    // FullName components (as per SDS: "Separate columns for FullName")
    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(length = 50)
    private String middleName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    // ContactInformation components
    @Column(name = "primary_phone_number", nullable = false, length = 20) // Uniqueness handled by partial index in DB + app logic
    private String primaryPhoneNumber;

    @Column(name = "secondary_phone_number", length = 20)
    private String secondaryPhoneNumber;

    @Column(name = "email_address", length = 100)
    private String emailAddress;

    @Embedded
    private AddressJpaEmbeddable address;

    @Column(columnDefinition = "geometry(Point,4326)") // SRID 4326 for WGS84
    private Point homesteadCoordinates;

    // NationalIdentification components
    @Column(length = 50)
    private String nationalIdType;

    @Convert(converter = JpaEncryptionConverter.class)
    @Column(name = "encrypted_national_id_number", length = 255) // Store encrypted value
    private String encryptedNationalIdNumber; // This string IS the EncryptedValue.value

    private Integer familySize;
    private Integer yearsOfFarmingExperience;

    @Column(length = 100)
    private String educationLevel; // Consider linking to a master data table via ID

    @Column(length = 50)
    private String preferredLanguage; // Consider linking to a master data table via ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FarmerStatus status;

    private LocalDateTime dateOfStatusChange;

    @Column(columnDefinition = "TEXT")
    private String reasonForStatusChange;

    @Column(length = 2048) // URL length
    private String profilePhotoUrl;

    @Embedded
    private AuditInfoJpaEmbeddable auditInfo;

    @OneToMany(mappedBy = "farmer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BankAccountJpaEntity> bankAccounts = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "farmer_memberships", joinColumns = @JoinColumn(name = "farmer_id"))
    @AttributeOverrides({
            @AttributeOverride(name = "organizationName", column = @Column(name = "organization_name", nullable = false, length = 150)),
            @AttributeOverride(name = "membershipId", column = @Column(name = "membership_identifier", length = 50))
    })
    private List<MembershipJpaEmbeddable> memberships = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "farmer_consents", joinColumns = @JoinColumn(name = "farmer_id"))
    private List<ConsentJpaEmbeddable> consents = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "farmer_approval_history", joinColumns = @JoinColumn(name = "farmer_id"))
    private List<ApprovalHistoryEntryJpaEmbeddable> approvalHistory = new ArrayList<>();

}