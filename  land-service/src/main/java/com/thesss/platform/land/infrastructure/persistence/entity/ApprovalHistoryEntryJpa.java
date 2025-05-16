package com.thesss.platform.land.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "approval_history")
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class ApprovalHistoryEntryJpa {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_land_record_id", referencedColumnName = "id", nullable = false)
    private FarmLandRecordJpaEntity farmLandRecord;

    @Column(name = "field_changed", nullable = false)
    private String fieldChanged;

    @Column(name = "previous_value", columnDefinition = "TEXT")
    private String previousValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "submitted_by", nullable = false)
    private String submittedBy;

    @Column(name = "submission_date", nullable = false)
    private Instant submissionDate;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approval_date")
    private Instant approvalDate;

    @Column(name = "comments", columnDefinition = "TEXT")
    private String comments;

    @CreatedBy
    @Column(name = "created_by_audit", updatable = false) // Renamed to avoid conflict
    private String createdByAudit;

    @CreatedDate
    @Column(name = "created_date_audit", updatable = false) // Renamed to avoid conflict
    private Instant createdDateAudit;

    @LastModifiedBy
    @Column(name = "last_modified_by_audit") // Renamed to avoid conflict
    private String lastModifiedByAudit;

    @LastModifiedDate
    @Column(name = "last_modified_date_audit") // Renamed to avoid conflict
    private Instant lastModifiedDateAudit;
}