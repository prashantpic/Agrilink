```java
package com.thesss.platform.workflows.utils;

public final class ProcessConstants {

    private ProcessConstants() {
        // Private constructor to prevent instantiation
    }

    // Process Definition Keys
    public static final String PROCESS_KEY_FARMER_REGISTRATION_APPROVAL = "farmerRegistrationApprovalProcess";
    public static final String PROCESS_KEY_LAND_CHANGE_APPROVAL = "landRecordChangeApprovalProcess";
    public static final String PROCESS_KEY_CRITICAL_DATA_APPROVAL = "criticalDataModificationApprovalProcess";
    public static final String PROCESS_KEY_SYNC_CONFLICT_RESOLUTION = "syncConflictResolutionProcess";

    // Process Variable Names
    public static final String VAR_APPROVAL_REQUEST_DATA = "approvalRequestData";
    public static final String VAR_APPROVAL_RESULT_DATA = "approvalResultData";
    public static final String VAR_SYNC_CONFLICT_DATA = "syncConflictData";
    public static final String VAR_IS_APPROVED = "isApproved";
    public static final String VAR_APPROVER_USER_ID = "approverUserId";
    public static final String VAR_APPROVAL_COMMENTS = "approvalComments";
    public static final String VAR_ENTITY_ID = "entityId";
    public static final String VAR_ENTITY_TYPE = "entityType";
    public static final String VAR_SUBMITTER_USER_ID = "submitterUserId";
    public static final String VAR_BUSINESS_KEY = "businessKey";


    // Delegate Bean Names (as referenced in BPMN)
    public static final String DELEGATE_SET_APPROVAL_OUTCOME = "setApprovalOutcomeDelegate";
    public static final String DELEGATE_UPDATE_FARMER_STATUS_COMMAND = "updateFarmerStatusCommandDelegate";
    public static final String DELEGATE_LOG_FARMER_APPROVAL_HISTORY = "logFarmerApprovalHistoryDelegate";
    public static final String DELEGATE_UPDATE_LAND_RECORD_STATUS_COMMAND = "updateLandRecordStatusCommandDelegate";
    public static final String DELEGATE_LOG_LAND_APPROVAL_HISTORY = "logLandApprovalHistoryDelegate";
    public static final String DELEGATE_SEND_APPROVAL_REQUEST_NOTIFICATION = "sendApprovalRequestNotificationDelegate";
    public static final String DELEGATE_SEND_WORKFLOW_STATUS_UPDATE_NOTIFICATION = "sendWorkflowStatusUpdateNotificationDelegate";
    public static final String DELEGATE_ASSIGN_APPROVER = "assignApproverDelegate";
    public static final String DELEGATE_APPLY_CONFLICT_RESOLUTION = "applyConflictResolutionDelegate";
    public static final String DELEGATE_APPLY_CRITICAL_DATA_MODIFICATION = "applyCriticalDataModificationDelegate";
    public static final String DELEGATE_LOG_CRITICAL_DATA_APPROVAL_HISTORY = "logCriticalDataApprovalHistoryDelegate";


    // Task Listener Bean Names
    public static final String LISTENER_USER_TASK_ASSIGNMENT = "userTaskAssignmentListener";

    // Message Names (if any)
    // public static final String MSG_NEW_APPROVAL_REQUEST = "Message_NewApprovalRequest";

    // Signal Names (if any)
    // public static final String SIGNAL_APPROVAL_ESCALATION = "Signal_ApprovalEscalation";

}
```