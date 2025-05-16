package com.thesss.platform.land.domain.model;

/**
 * Enum representing the status of a farm land record.
 * Values are typically codes that map to fuller descriptions in Master Data.
 * REQ-2-019
 */
public enum LandRecordStatus {
    DRAFT,              // Initial state, not yet submitted for approval or use
    PENDING_APPROVAL,   // Submitted and awaiting approval
    ACTIVE,             // Approved and in active use or management
    INACTIVE,           // Temporarily not in use, but still part of records
    DISPUTED,           // Ownership or boundary is under dispute
    UNDER_CULTIVATION,  // Specifically marked as being cultivated
    FALLOW,             // Intentionally left uncultivated for a period
    SOLD,               // Land has been sold and is no longer managed by the current farmer in this context
    REJECTED            // Submitted for approval but was rejected
}