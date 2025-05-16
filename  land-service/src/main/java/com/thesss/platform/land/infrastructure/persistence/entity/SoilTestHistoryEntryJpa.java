package com.thesss.platform.land.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "soil_test_history")
@Getter
@Setter
public class SoilTestHistoryEntryJpa {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_land_record_id", referencedColumnName = "id", nullable = false)
    private FarmLandRecordJpaEntity farmLandRecord;

    @Column(name = "test_date", nullable = false)
    private LocalDate testDate;

    @Column(name = "ph")
    private Double pH;

    @Column(name = "nitrogen_value")
    private Double nitrogenValue;

    @Column(name = "nitrogen_unit")
    private String nitrogenUnit;

    @Column(name = "phosphorus_value")
    private Double phosphorusValue;

    @Column(name = "phosphorus_unit")
    private String phosphorusUnit;

    @Column(name = "potassium_value")
    private Double potassiumValue;

    @Column(name = "potassium_unit")
    private String potassiumUnit;

    @Column(name = "micronutrients", columnDefinition = "TEXT")
    private String micronutrients;

    @Column(name = "test_report_url")
    private String testReportUrl;
}