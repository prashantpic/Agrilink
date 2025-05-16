package com.thesss.platform.farmer.domain.model;

/**
 * Enum representing farmer status.
 * REQ-FRM-018
 */
public enum FarmerStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    PENDING_APPROVAL("Pending Approval"), // For new registrations or critical updates
    APPROVED("Approved"), // Intermediate status after approval, might transition to ACTIVE
    SUSPENDED("Suspended"),
    DECEASED("Deceased");

    private final String displayName;

    FarmerStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static FarmerStatus fromString(String statusStr) {
        for (FarmerStatus status : FarmerStatus.values()) {
            if (status.name().equalsIgnoreCase(statusStr) || status.getDisplayName().equalsIgnoreCase(statusStr)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown farmer status: " + statusStr);
    }
}