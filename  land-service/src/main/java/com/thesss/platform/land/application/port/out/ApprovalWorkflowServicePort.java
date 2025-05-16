package com.thesss.platform.land.application.port.out;

import com.thesss.platform.land.domain.model.FarmerId;
import com.thesss.platform.land.domain.model.LandRecordId;

// This is a placeholder interface definition.
// Implementations will interact with the Approval Workflow Orchestrator via HTTP.
public interface ApprovalWorkflowServicePort { // REQ-2-021, REQ-2-004

    /**
     * Initiates an approval workflow for a change related to a land record.
     *
     * @param landRecordId The ID of the land record.
     * @param farmerId     The ID of the associated farmer.
     * @param changeSummary A description of the change requiring approval (e.g., "Status change to Active", "Parcel ID updated").
     * @return A workflow instance ID or similar identifier.
     */
    String initiateLandRecordApproval(LandRecordId landRecordId, FarmerId farmerId, String changeSummary);

    // Other methods might include tracking status, approving/rejecting (potentially inbound from orchestrator), etc.
}