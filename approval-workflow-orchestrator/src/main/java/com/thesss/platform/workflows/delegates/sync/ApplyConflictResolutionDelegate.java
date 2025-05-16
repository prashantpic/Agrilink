package com.thesss.platform.workflows.delegates.sync;

import com.thesss.platform.workflows.client.farmer.FarmerServiceClient;
import com.thesss.platform.workflows.client.land.LandServiceClient;
// Assuming FarmerServiceClient and LandServiceClient have methods like:
// void applyFarmerConflictResolution(String farmerId, FarmerConflictResolutionDto resolutionDto);
// void applyLandConflictResolution(String landId, LandConflictResolutionDto resolutionDto);
// And corresponding DTOs. For now, let's assume a generic Map<String, Object> or a String for resolvedData.
// Let's define placeholder DTOs for clarity.
import com.thesss.platform.workflows.client.farmer.dto.FarmerConflictResolutionRequestDto;
import com.thesss.platform.workflows.client.land.dto.LandConflictResolutionRequestDto;

import com.thesss.platform.workflows.model.process.SyncConflictData;
import com.thesss.platform.workflows.utils.ProcessConstants;
import com.fasterxml.jackson.databind.ObjectMapper; // For converting resolvedData string to specific DTO

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("applyConflictResolutionDelegate")
@RequiredArgsConstructor
public class ApplyConflictResolutionDelegate implements JavaDelegate {

    private final FarmerServiceClient farmerServiceClient;
    private final LandServiceClient landServiceClient;
    private final ObjectMapper objectMapper; // For converting resolvedData JSON string

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("Executing ApplyConflictResolutionDelegate for process instance {}", execution.getProcessInstanceId());

        SyncConflictData syncConflictData = (SyncConflictData) execution.getVariable(ProcessConstants.VAR_SYNC_CONFLICT_DATA);

        if (syncConflictData == null) {
            log.error("SyncConflictData not found for instance {}. Cannot apply resolution.", execution.getProcessInstanceId());
            throw new BpmnError("VAR_MISSING_ERROR", "SyncConflictData not found.");
        }

        String entityType = syncConflictData.getEntityType();
        String entityId = syncConflictData.getEntityId();
        String resolvedDataJson = syncConflictData.getResolvedData(); // This is a JSON string as per SDS
        String resolvedByUserId = syncConflictData.getResolvedByUserId();

        if (resolvedDataJson == null || resolvedDataJson.isEmpty()) {
            log.error("ResolvedData is null or empty for conflictId {}. Cannot apply resolution.", syncConflictData.getConflictId());
            throw new BpmnError("RESOLVED_DATA_MISSING", "Resolved data is missing for conflict " + syncConflictData.getConflictId());
        }

        try {
            if (ProcessConstants.ENTITY_TYPE_FARMER.equals(entityType)) {
                FarmerConflictResolutionRequestDto farmerResolutionDto = objectMapper.readValue(resolvedDataJson, FarmerConflictResolutionRequestDto.class);
                // farmerResolutionDto.setResolvedByUserId(resolvedByUserId); // If DTO needs it
                log.info("Applying conflict resolution for Farmer entityId: {}", entityId);
                farmerServiceClient.applyFarmerConflictResolution(entityId, farmerResolutionDto);
                log.info("Successfully applied conflict resolution for Farmer entityId: {}", entityId);

            } else if (ProcessConstants.ENTITY_TYPE_LAND_RECORD.equals(entityType)) {
                LandConflictResolutionRequestDto landResolutionDto = objectMapper.readValue(resolvedDataJson, LandConflictResolutionRequestDto.class);
                // landResolutionDto.setResolvedByUserId(resolvedByUserId); // If DTO needs it
                log.info("Applying conflict resolution for LandRecord entityId: {}", entityId);
                landServiceClient.applyLandConflictResolution(entityId, landResolutionDto);
                log.info("Successfully applied conflict resolution for LandRecord entityId: {}", entityId);

            } else {
                log.error("Unsupported entity type '{}' for conflict resolution. ConflictId: {}", entityType, syncConflictData.getConflictId());
                throw new BpmnError("UNSUPPORTED_ENTITY_TYPE_CONFLICT", "Unsupported entity type for conflict resolution: " + entityType);
            }
        } catch (Exception e) {
            log.error("Failed to apply conflict resolution for entityType {}, entityId {}: {}", entityType, entityId, e.getMessage(), e);
            throw new BpmnError("CONFLICT_RESOLUTION_APPLY_FAILED", "Failed to apply conflict resolution: " + e.getMessage());
        }
    }
}