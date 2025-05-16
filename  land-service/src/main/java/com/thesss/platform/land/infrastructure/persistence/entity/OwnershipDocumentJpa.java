package com.thesss.platform.land.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "ownership_document")
@Getter
@Setter
public class OwnershipDocumentJpa {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_land_record_id", referencedColumnName = "id", nullable = false)
    private FarmLandRecordJpaEntity farmLandRecord;

    @Column(name = "document_url", nullable = false)
    private String documentUrl;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;
}