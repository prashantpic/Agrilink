package com.thesss.platform.farmer.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.EntityListeners;


import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@EntityListeners(AuditingEntityListener.class)
public class AuditInfoJpaEmbeddable {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateOfRegistration; // Maps to createdDate

    @CreatedBy
    @Column(nullable = false, updatable = false, length = 50)
    private String registeredBy; // Maps to createdBy

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastUpdatedDate;

    @LastModifiedBy
    @Column(nullable = false, length = 50)
    private String lastUpdatedBy;
}