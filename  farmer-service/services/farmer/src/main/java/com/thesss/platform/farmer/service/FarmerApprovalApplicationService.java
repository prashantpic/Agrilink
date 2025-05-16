package com.thesss.platform.farmer.service;

import com.thesss.platform.farmer.domain.model.ApprovalHistoryEntry;
import com.thesss.platform.farmer.domain.model.Farmer;
import com.thesss.platform.farmer.domain.model.FarmerId;
import com.thesss.platform.farmer.domain.model.FarmerStatus;
import com.thesss.platform.farmer.domain.repository.FarmerRepository;
import com.thesss.platform.farmer.exception.FarmerNotFoundException;
import com.thesss.platform.farmer.service.dto.ApprovalHistoryEntryDto; // Assuming this DTO exists
import com.thesss.platform.farmer.service.mapper.FarmerDtoMapper; // Assuming this mapper exists

// import com.thesss.platform.farmer.domain.event.CriticalFieldChangeSubmittedForApprovalEvent;
// import org.springframework.context.ApplicationEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FarmerApprovalApplicationService {

    private final FarmerRepository farmerRepository;
    private final FarmerDtoMapper farmerDtoMapper; // For mapping ApprovalHistoryEntry to DTO
    // private final ApplicationEventPublisher eventPublisher; // If using Spring events for decoupling

    // Assuming an external approval orchestrator might be called via HTTP client or messaging
    // private final ApprovalOrchestratorClient approvalOrchestratorClient;

    @Transactional
    public void submitForApproval(Farmer farmer, Map<String, Object> changedFields, String submittedByUserId) {
        log.info("Submitting changes for farmer ID: {} for approval by user: {}", farmer.getFarmerId().getValue(), submittedByUserId);

        // Create a new approval history entry for the submission
        // For new registration, 'changedFields' might be a special map, 'previousValue' null.
        // For updates, iterate through changedFields.
        changedFields.forEach((field, newValue) -> {
            ApprovalHistoryEntry entry = ApprovalHistoryEntry.builder()
                    .farmerId(farmer.getFarmerId()) // Link back to farmer
                    .fieldNameChanged(field)
                    .newValue(newValue != null ? newValue.toString() : null)
                    // .previousValue(...) // Logic to get previous value, if applicable for update
                    .submittedByUserId(submittedByUserId)
                    .submissionDate(LocalDateTime.now())
                    .approvalStatus("PENDING_APPROVAL") // Initial status
                    .build();
            farmer.addApprovalEntry(entry); // Add to farmer's approval history
        });

        // Change farmer status to reflect pending approval for the update/registration
        // This might be more nuanced depending on whether it's a new registration or an update.
        if (farmer.getStatus() != FarmerStatus.PENDING_APPROVAL) {
             farmer.changeStatus(FarmerStatus.PENDING_APPROVAL, "Submitted for approval of changes.", submittedByUserId);
        }

        farmerRepository.save(farmer);

        // Publish an event or call an external approval orchestrator
        // CriticalFieldChangeSubmittedForApprovalEvent event = new CriticalFieldChangeSubmittedForApprovalEvent(
        // farmer.getFarmerId(),
        // changedFields, // Or a more structured ChangeDetails object
        // submittedByUserId,
        // LocalDateTime.now()
        // );
        // eventPublisher.publishEvent(event);
        // log.info("Published CriticalFieldChangeSubmittedForApprovalEvent for farmer {}", farmer.getFarmerId().getValue());

        // Or direct call:
        // approvalOrchestratorClient.submit(farmer.getFarmerId(), changedFields, submittedByUserId);
        log.info("Changes for farmer {} submitted to approval system (simulated).", farmer.getFarmerId().getValue());
    }

    @Transactional
    public void handleApprovalOutcome(UUID farmerIdValue, UUID approvalInstanceId, String outcome,
                                      String approvedRejectedByUserId, String comments) {
        log.info("Handling approval outcome for farmer ID: {}. Outcome: {}, By: {}, Comments: {}",
                farmerIdValue, outcome, approvedRejectedByUserId, comments);
        FarmerId farmerId = FarmerId.of(farmerIdValue);
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new FarmerNotFoundException(farmerIdValue));

        // Find the relevant PENDING_APPROVAL entry in history and update it
        // This logic needs to be robust, perhaps linking by approvalInstanceId
        farmer.getApprovalHistory().stream()
                .filter(entry -> "PENDING_APPROVAL".equals(entry.getApprovalStatus())) // Simplified match
                // .filter(entry -> entry.getApprovalInstanceId().equals(approvalInstanceId)) // If an ID is passed back
                .forEach(entry -> {
                    entry.setApprovalStatus(outcome); // e.g., "APPROVED", "REJECTED"
                    entry.setApprovedRejectedByUserId(approvedRejectedByUserId);
                    entry.setApprovalRejectionDate(LocalDateTime.now());
                    entry.setApproverComments(comments);
                });

        FarmerStatus newFarmerStatus;
        String statusChangeReason = "Approval outcome: " + outcome + ". " + comments;

        if ("APPROVED".equalsIgnoreCase(outcome)) {
            // Determine if this was initial registration or an update approval
            // If it was PENDING_APPROVAL from registration, new status is ACTIVE (or APPROVED then ACTIVE)
            // If it was PENDING_APPROVAL for an update, revert to previous status or ACTIVE
            newFarmerStatus = FarmerStatus.ACTIVE; // Or FarmerStatus.APPROVED, then another step to ACTIVE
            // Potentially publish FarmerRegisteredEvent if it's the final approval of a new farmer
            // if (farmer.getStatus() == FarmerStatus.PENDING_APPROVAL && isInitialRegistration) {
            // eventPublisher.publishEvent(new FarmerRegisteredEvent(farmer.getFarmerId(), LocalDateTime.now()));
            // }
        } else if ("REJECTED".equalsIgnoreCase(outcome)) {
            // If registration rejected, farmer might go to INACTIVE or a specific "REJECTED" status
            // If update rejected, farmer might revert to previous status before PENDING_APPROVAL
            newFarmerStatus = FarmerStatus.INACTIVE; // Example, could be more nuanced
        } else {
            log.warn("Unhandled approval outcome: {} for farmer {}", outcome, farmerIdValue);
            // Potentially keep as PENDING_APPROVAL or move to a specific error status
            return; // Or throw exception
        }

        farmer.changeStatus(newFarmerStatus, statusChangeReason, approvedRejectedByUserId);
        farmerRepository.save(farmer);
        log.info("Farmer {} status updated to {} after approval outcome.", farmerIdValue, newFarmerStatus);
    }

    @Transactional
    public void logAdminModification(Farmer farmer, Map<String, Object> changedFields,
                                     String adminUserId, String comments) {
        log.info("Logging admin modification for farmer ID: {} by admin: {}", farmer.getFarmerId().getValue(), adminUserId);

        changedFields.forEach((field, newValue) -> {
            ApprovalHistoryEntry entry = ApprovalHistoryEntry.builder()
                    .farmerId(farmer.getFarmerId())
                    .fieldNameChanged(field)
                    .newValue(newValue != null ? newValue.toString() : null)
                    // .previousValue(...) // Could fetch previous value if needed for logs
                    .submittedByUserId(adminUserId) // Admin is the submitter
                    .submissionDate(LocalDateTime.now())
                    .approvalStatus("ADMIN_MODIFIED") // Special status for direct admin changes
                    .approvedRejectedByUserId(adminUserId) // Admin is also the approver
                    .approvalRejectionDate(LocalDateTime.now())
                    .approverComments(comments != null ? comments : "Direct modification by administrator.")
                    .build();
            farmer.addApprovalEntry(entry);
        });
        // The farmer's actual data changes are assumed to be handled by the calling service (FarmerRegistryApplicationService)
        // This method focuses on logging the admin action in the approval history.
        // farmerRepository.save(farmer); // The calling service will save the farmer.
    }


    @Transactional(readOnly = true)
    public List<ApprovalHistoryEntryDto> getApprovalHistory(UUID farmerIdValue) {
        log.debug("Fetching approval history for farmer ID: {}", farmerIdValue);
        FarmerId farmerId = FarmerId.of(farmerIdValue);
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new FarmerNotFoundException(farmerIdValue));

        return farmer.getApprovalHistory().stream()
                .map(farmerDtoMapper::toApprovalHistoryEntryDto) // Assuming mapper exists
                .collect(Collectors.toList());
    }
}