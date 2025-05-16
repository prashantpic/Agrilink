package com.thesss.platform.farmer.domain.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value object representing membership in a farmer organization.
 * REQ-FRM-014
 */
public final class Membership implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String organizationName;
    private final String membershipId; // Optional

    public Membership(String organizationName, String membershipId) {
        if (organizationName == null || organizationName.isBlank()) {
            throw new IllegalArgumentException("Organization name cannot be blank.");
        }
        this.organizationName = organizationName;
        this.membershipId = membershipId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String getMembershipId() {
        return membershipId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Membership that = (Membership) o;
        return Objects.equals(organizationName, that.organizationName) &&
               Objects.equals(membershipId, that.membershipId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(organizationName, membershipId);
    }

    @Override
    public String toString() {
        return "Membership{" +
               "organizationName='" + organizationName + '\'' +
               ", membershipId='" + membershipId + '\'' +
               '}';
    }
}