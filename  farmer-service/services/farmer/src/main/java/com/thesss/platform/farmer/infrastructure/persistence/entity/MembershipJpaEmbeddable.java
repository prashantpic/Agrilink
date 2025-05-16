package com.thesss.platform.farmer.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class MembershipJpaEmbeddable {

    @Column(name = "organization_name", nullable = false, length = 150)
    private String organizationName;

    @Column(name = "membership_identifier", length = 50)
    private String membershipId; // Optional
}