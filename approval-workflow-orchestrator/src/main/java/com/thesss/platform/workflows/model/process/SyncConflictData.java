package com.thesss.platform.workflows.model.process;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncConflictData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String conflictId;
    private String entityType;
    private String entityId;
    private Map<String, Object> conflictingFields; // Can store complex objects or just changed fields
    private String offlineVersionDetails; // Could be JSON string of the offline entity
    private String serverVersionDetails;  // Could be JSON string of the server entity
    private String resolutionType; // e.g., "TAKE_SERVER", "TAKE_OFFLINE", "MERGE_MANUAL"
    private String resolvedData;   // JSON string of the resolved entity after user intervention
    private String resolvedByUserId;
}