package com.thesss.platform.workflows.api.controller;

import com.thesss.platform.workflows.api.dto.CriticalDataApprovalRequestDto;
import com.thesss.platform.workflows.api.dto.FarmerRegistrationApprovalRequestDto;
import com.thesss.platform.workflows.api.dto.LandChangeApprovalRequestDto;
import com.thesss.platform.workflows.api.dto.ProcessInstanceResponseDto;
import com.thesss.platform.workflows.api.dto.SyncConflictResolutionRequestDto;
import com.thesss.platform.workflows.service.WorkflowOrchestrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workflows/approvals")
@RequiredArgsConstructor
public class ApprovalWorkflowController {

    private final WorkflowOrchestrationService workflowOrchestrationService;

    @PostMapping("/farmer-registration")
    public ResponseEntity<ProcessInstanceResponseDto> startFarmerRegistrationApproval(
            @RequestBody @Valid FarmerRegistrationApprovalRequestDto requestDto) {
        ProcessInstanceResponseDto response = workflowOrchestrationService.startFarmerRegistrationApprovalWorkflow(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/land-change")
    public ResponseEntity<ProcessInstanceResponseDto> startLandChangeApproval(
            @RequestBody @Valid LandChangeApprovalRequestDto requestDto) {
        ProcessInstanceResponseDto response = workflowOrchestrationService.startLandChangeApprovalWorkflow(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/critical-data")
    public ResponseEntity<ProcessInstanceResponseDto> startCriticalDataModificationApproval(
            @RequestBody @Valid CriticalDataApprovalRequestDto requestDto) {
        ProcessInstanceResponseDto response = workflowOrchestrationService.startCriticalDataApprovalWorkflow(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/sync-conflict")
    public ResponseEntity<ProcessInstanceResponseDto> startSyncConflictResolution(
            @RequestBody @Valid SyncConflictResolutionRequestDto requestDto) {
        ProcessInstanceResponseDto response = workflowOrchestrationService.startSyncConflictResolutionWorkflow(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}