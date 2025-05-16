package com.thesss.platform.crop.domain.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import java.util.Objects;

@Embeddable
public class CropCycleStatusInfo {

    @Column(name = "status_master_id", nullable = false)
    private String statusMasterId; // References MasterData ID for status (e.g., PLANNED, SOWN, HARVESTED, FAILED)

    @Column(name = "reason_for_failure", columnDefinition = "TEXT")
    private String reasonForFailure; // Optional text

    protected CropCycleStatusInfo() {
        // JPA LOMBOK Requirement
    }

    public CropCycleStatusInfo(String statusMasterId, String reasonForFailure) {
        if (statusMasterId == null || statusMasterId.isBlank()) {
            throw new IllegalArgumentException("Status master ID cannot be null or empty");
        }
        this.statusMasterId = statusMasterId;
        this.reasonForFailure = reasonForFailure;
    }

    public String getStatusMasterId() {
        return statusMasterId;
    }

    public String getReasonForFailure() {
        return reasonForFailure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CropCycleStatusInfo that = (CropCycleStatusInfo) o;
        return Objects.equals(statusMasterId, that.statusMasterId) &&
               Objects.equals(reasonForFailure, that.reasonForFailure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusMasterId, reasonForFailure);
    }
}