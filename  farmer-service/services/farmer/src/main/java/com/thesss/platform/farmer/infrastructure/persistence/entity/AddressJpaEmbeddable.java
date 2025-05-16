package com.thesss.platform.farmer.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AddressJpaEmbeddable {

    @Column(name = "street_address_village", length = 200)
    private String streetAddressVillage;

    @Column(name = "tehsil_taluk_block", length = 100)
    private String tehsilTalukBlock;

    @Column(length = 100)
    private String district;

    @Column(name = "state_province", length = 100)
    private String stateProvince;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(length = 100)
    private String country;
}