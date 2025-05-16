package com.thesss.platform.farmer.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class ConsentJpaEmbeddable {

    @Column(nullable = false)
    private boolean consentGiven;

    @Column(nullable = false)
    private LocalDateTime consentTimestamp;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String consentPurpose;

    @Column(nullable = false, length = 50)
    private String consentVersionId;
}