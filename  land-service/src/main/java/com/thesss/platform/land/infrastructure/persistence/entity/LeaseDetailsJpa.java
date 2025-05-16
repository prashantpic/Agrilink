package com.thesss.platform.land.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "lease_details")
@Getter
@Setter
public class LeaseDetailsJpa {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "farm_land_record_id", referencedColumnName = "id", nullable = false)
    private FarmLandRecordJpaEntity farmLandRecord;

    @Column(name = "lessor_name")
    private String lessorName;

    @Column(name = "lessor_contact")
    private String lessorContact;

    @Column(name = "lease_start_date")
    private LocalDate leaseStartDate;

    @Column(name = "lease_end_date")
    private LocalDate leaseEndDate;

    @Column(name = "lease_terms", columnDefinition = "TEXT")
    private String leaseTerms;
}