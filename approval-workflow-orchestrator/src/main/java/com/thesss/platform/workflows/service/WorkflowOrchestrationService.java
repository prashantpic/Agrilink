package com.thesss.platform.workflows.service;

import com.thesss.platform.workflows.api.dto.CriticalDataApprovalRequestDto;
import com.thesss.platform.workflows.api.dto.FarmerRegistrationApprovalRequestDto;
import com.thesss.platform.workflows.api.dto.LandChangeApprovalRequestDto;
import com.thesss.platform.workflows.api.dto.SyncConflictResolutionRequestDto;
import com.thesss.platform.workflows.model.process.ApprovalRequestData;
import com.thesss.platform.workflows.model.process.SyncConflictData;
import com.thesss.platform.workflows.utils.ProcessConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowOrchestrationService {

    private final RuntimeService runtimeService;

    @Transactional
    public ProcessInstance startFarmerRegistrationApprovalProcess(FarmerRegistrationApprovalRequestDto requestDto) {
        log.info("Starting farmer registration approval process for entityId: {}", requestDto.getFarmerId());
        ApprovalRequestData approvalRequestData = ApprovalRequestData.builder()
                .entityId(requestDto.getFarmerId())
                .entityType(ProcessConstants.ENTITY_TYPE_FARMER)
                .submittedByUserId(requestDto.getSubmittedByUserId())
                .submissionDate(OffsetDateTime.now())
                .changes(requestDto.getChanges())
                .workflowDefinitionKey(ProcessConstants.PROCESS_KEY_FARMER_REGISTRATION_APPROVAL)
                .businessKey(generateBusinessKey(requestDto.getFarmerId()))
                .build();

        Map<String, Object> variables = new HashMap<>();
        variables.put(ProcessConstants.VAR_APPROVAL_REQUEST_DATA, approvalRequestData);

        return runtimeService.startProcessInstanceByKey(
                ProcessConstants.PROCESS_KEY_FARMER_REGISTRATION_APPROVAL,
                approvalRequestData.getBusinessKey(),
                variables
        );
    }

    @Transactional
    public ProcessInstance startLandChangeApprovalProcess(LandChangeApprovalRequestDto requestDto) {
        log.info("Starting land change approval process for entityId: {}", requestDto.getLandRecordId());
        ApprovalRequestData approvalRequestData = ApprovalRequestData.builder()
                .entityId(requestDto.getLandRecordId())
                .entityType(ProcessConstants.ENTITY_TYPE_LAND_RECORD)
                .submittedByUserId(requestDto.getSubmittedByUserId())
                .submissionDate(OffsetDateTime.now())
                .changes(requestDto.getChanges())
                .workflowDefinitionKey(ProcessConstants.PROCESS_KEY_LAND_RECORD_CHANGE_APPROVAL)
                .businessKey(generateBusinessKey(requestDto.getLandRecordId()))
                .build();

        Map<String, Object> variables = new HashMap<>();
        variables.put(ProcessConstants.VAR_APPROVAL_REQUEST_DATA, approvalRequestData);

        return runtimeService.startProcessInstanceByKey(
                ProcessConstants.PROCESS_KEY_LAND_RECORD_CHANGE_APPROVAL,
                approvalRequestData.getBusinessKey(),
                variables
        );
    }

    @Transactional
    public ProcessInstance startCriticalDataApprovalProcess(CriticalDataApprovalRequestDto requestDto) {
        log.info("Starting critical data approval process for entityType: {}, entityId: {}", requestDto.getEntityType(), requestDto.getEntityId());
        ApprovalRequestData approvalRequestData = ApprovalRequestData.builder()
                .entityId(requestDto.getEntityId())
                .entityType(requestDto.getEntityType())
                .submittedByUserId(requestDto.getSubmittedByUserId())
                .submissionDate(OffsetDateTime.now())
                .changes(requestDto.getChanges())
                .workflowDefinitionKey(ProcessConstants.PROCESS_KEY_CRITICAL_DATA_MODIFICATION_APPROVAL)
                .businessKey(generateBusinessKey(requestDto.getEntityType() + "_" + requestDto.getEntityId()))
                .build();

        Map<String, Object> variables = new HashMap<>();
        variables.put(ProcessConstants.VAR_APPROVAL_REQUEST_DATA, approvalRequestData);

        return runtimeService.startProcessInstanceByKey(
                ProcessConstants.PROCESS_KEY_CRITICAL_DATA_MODIFICATION_APPROVAL,
                approvalRequestData.getBusinessKey(),
                variables
        );
    }

    @Transactional
    public ProcessInstance startSyncConflictResolutionProcess(SyncConflictResolutionRequestDto requestDto) {
        log.info("Starting sync conflict resolution process for conflictId: {}", requestDto.getConflictId());
        SyncConflictData syncConflictData = SyncConflictData.builder()
                .conflictId(requestDto.getConflictId())
                .entityType(requestDto.getEntityType())
                .entityId(requestDto.getEntityId())
                .conflictingFields(requestDto.getConflictingFields())
                .offlineVersionDetails(requestDto.getOfflineVersionDetails())
                .serverVersionDetails(requestDto.getServerVersionDetails())
                .submittedByUserId(requestDto.getSubmittedByUserId()) // Assuming submitter for initiating workflow
                .build();
        
        String businessKey = generateBusinessKey("SYNC_CONFLICT_" + requestDto.getConflictId());

        Map<String, Object> variables = new HashMap<>();
        variables.put(ProcessConstants.VAR_SYNC_CONFLICT_DATA, syncConflictData);

        return runtimeService.startProcessInstanceByKey(
                ProcessConstants.PROCESS_KEY_SYNC_CONFLICT_RESOLUTION,
                businessKey,
                variables
        );
    }

    private String generateBusinessKey(String entityId) {
        return entityId + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}