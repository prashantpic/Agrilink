package com.thesss.platform.farmer.domain.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value object representing a physical address.
 * REQ-FRM-008
 */
public final class Address implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String streetAddressVillage;
    private final String tehsilTalukBlock;
    private final String district;
    private final String stateProvince;
    private final String postalCode;
    private final String country;

    public Address(String streetAddressVillage, String tehsilTalukBlock, String district,
                   String stateProvince, String postalCode, String country) {
        // Basic validation, can be enhanced based on specific country rules
        if (country == null || country.isBlank()) {
            // Allow if address is fully optional. If not, throw IllegalArgumentException.
            // For now, assuming individual fields can be blank but if country is given, it shouldn't be blank.
        }
        this.streetAddressVillage = streetAddressVillage;
        this.tehsilTalukBlock = tehsilTalukBlock;
        this.district = district;
        this.stateProvince = stateProvince;
        this.postalCode = postalCode;
        this.country = country;
    }

    public String getStreetAddressVillage() {
        return streetAddressVillage;
    }

    public String getTehsilTalukBlock() {
        return tehsilTalukBlock;
    }

    public String getDistrict() {
        return district;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(streetAddressVillage, address.streetAddressVillage) &&
               Objects.equals(tehsilTalukBlock, address.tehsilTalukBlock) &&
               Objects.equals(district, address.district) &&
               Objects.equals(stateProvince, address.stateProvince) &&
               Objects.equals(postalCode, address.postalCode) &&
               Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streetAddressVillage, tehsilTalukBlock, district, stateProvince, postalCode, country);
    }

    @Override
    public String toString() {
        return "Address{" +
               "streetAddressVillage='" + streetAddressVillage + '\'' +
               ", tehsilTalukBlock='" + tehsilTalukBlock + '\'' +
               ", district='" + district + '\'' +
               ", stateProvince='" + stateProvince + '\'' +
               ", postalCode='" + postalCode + '\'' +
               ", country='" + country + '\'' +
               '}';
    }
}