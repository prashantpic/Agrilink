package com.thesss.platform.farmer.service;

import com.thesss.platform.farmer.domain.model.Farmer;
import com.thesss.platform.farmer.domain.model.FarmerId;
import com.thesss.platform.farmer.domain.model.FarmerStatus;
import com.thesss.platform.farmer.domain.repository.FarmerRepository;
import com.thesss.platform.farmer.domain.repository.FarmerSearchCriteria; // Assuming this DTO exists
import com.thesss.platform.farmer.domain.service.FarmerFactory;
import com.thesss.platform.farmer.domain.service.EncryptionService;
import com.thesss.platform.farmer.exception.DuplicateFarmerResourceException;
import com.thesss.platform.farmer.exception.FarmerNotFoundException;
import com.thesss.platform.farmer.exception.InvalidRequestException;
import com.thesss.platform.farmer.service.dto.FarmerCreateDto;
import com.thesss.platform.farmer.service.dto.FarmerDetailsDto;
import com.thesss.platform.farmer.service.dto.FarmerUpdateDto;
import com.thesss.platform.farmer.service.mapper.FarmerDtoMapper;

// Domain Events - Assuming these exist
// import com.thesss.platform.farmer.domain.event.FarmerRegisteredEvent;
// import com.thesss.platform.farmer.domain.event.CriticalFieldChangeSubmittedForApprovalEvent;
// import org.springframework.context.ApplicationEventPublisher; // For publishing events

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class FarmerRegistryApplicationService {

    private final FarmerRepository farmerRepository;
    private final FarmerFactory farmerFactory;
    private final EncryptionService encryptionService; // Used by FarmerFactory, and potentially here for updates
    private final FarmerDtoMapper farmerDtoMapper;
    private final FarmerApprovalApplicationService farmerApprovalApplicationService;
    // private final ApplicationEventPublisher eventPublisher; // If using Spring events

    @Transactional
    public FarmerDetailsDto registerFarmer(FarmerCreateDto farmerCreateDto, String submittingUserId) {
        log.info("Registering new farmer submitted by user: {}", submittingUserId);

        // REQ-FRM-006: Unique primary phone number check for ACTIVE/PENDING_APPROVAL farmers
        if (farmerCreateDto.getContactInformation() != null && farmerCreateDto.getContactInformation().getPrimaryPhoneNumber() != null) {
            boolean exists = farmerRepository.existsByPrimaryPhoneNumberAndStatusIn(
                    farmerCreateDto.getContactInformation().getPrimaryPhoneNumber(),
                    List.of(FarmerStatus.ACTIVE, FarmerStatus.PENDING_APPROVAL)
            );
            if (exists) {
                log.warn("Attempt to register farmer with duplicate primary phone number: {}", farmerCreateDto.getContactInformation().getPrimaryPhoneNumber());
                throw new DuplicateFarmerResourceException("Primary phone number", farmerCreateDto.getContactInformation().getPrimaryPhoneNumber());
            }
        } else {
            throw new InvalidRequestException("Primary phone number is required for registration.");
        }

        Farmer farmer = farmerFactory.createFarmer(farmerCreateDto, submittingUserId);
        Farmer savedFarmer = farmerRepository.save(farmer);

        // Handle approval workflow initiation (REQ-FRM-023)
        // Assuming new registrations go to PENDING_APPROVAL by default from FarmerFactory
        if (savedFarmer.getStatus() == FarmerStatus.PENDING_APPROVAL) {
            // Example: Submit all fields as "changed" for a new registration approval
            Map<String, Object> initialFields = Map.of("newRegistration", true); // Or be more specific
             farmerApprovalApplicationService.submitForApproval(savedFarmer, initialFields, submittingUserId);
            // Or publish an event:
            // eventPublisher.publishEvent(new CriticalFieldChangeSubmittedForApprovalEvent(
            // savedFarmer.getFarmerId(), initialFields, submittingUserId, LocalDateTime.now()));
            log.info("Farmer registration for {} submitted for approval.", savedFarmer.getFarmerId().getValue());
        } else if (savedFarmer.getStatus() == FarmerStatus.APPROVED || savedFarmer.getStatus() == FarmerStatus.ACTIVE) {
            // If admin registration or auto-approval (REQ-FRM-024)
            // farmerApprovalApplicationService.logAdminModification(savedFarmer.getFarmerId().getValue(), Map.of("newRegistration", "Admin/Auto Approved"), submittingUserId, "Initial registration approved directly.");
            // eventPublisher.publishEvent(new FarmerRegisteredEvent(savedFarmer.getFarmerId(), LocalDateTime.now()));
            log.info("Farmer {} registered and approved directly.", savedFarmer.getFarmerId().getValue());
        }


        return farmerDtoMapper.toFarmerDetailsDto(savedFarmer);
    }

    @Transactional(readOnly = true)
    public FarmerDetailsDto getFarmerById(UUID farmerIdValue) {
        log.debug("Fetching farmer by ID: {}", farmerIdValue);
        FarmerId farmerId = FarmerId.of(farmerIdValue);
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new FarmerNotFoundException(farmerIdValue));
        // Decryption for display is typically handled by the mapper or not done if EncryptedValue is exposed.
        // For internal use, if plaintext is needed, it would be decrypted here.
        // This DTO might carry EncryptedValue for sensitive fields, mapper to API DTO will mask.
        return farmerDtoMapper.toFarmerDetailsDto(farmer);
    }

    @Transactional
    public FarmerDetailsDto updateFarmer(UUID farmerIdValue, FarmerUpdateDto farmerUpdateDto, String submittingUserId, boolean isAdminUpdate) {
        log.info("Updating farmer ID: {} by user: {}, isAdminUpdate: {}", farmerIdValue, submittingUserId, isAdminUpdate);
        FarmerId farmerId = FarmerId.of(farmerIdValue);
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new FarmerNotFoundException(farmerIdValue));

        // Logic to identify critical vs non-critical field changes
        // For simplicity, assume farmerUpdateDto contains only changed fields or a way to detect them.
        // This would involve comparing farmerUpdateDto with the current farmer state.
        Map<String, Object> changedFields = identifyChangedFields(farmer, farmerUpdateDto); // Placeholder method

        // REQ-FRM-006: If primary phone number is being changed, check uniqueness
        if (farmerUpdateDto.getContactInformation() != null &&
            farmerUpdateDto.getContactInformation().getPrimaryPhoneNumber() != null &&
            !farmerUpdateDto.getContactInformation().getPrimaryPhoneNumber().equals(farmer.getContactInformation().getPrimaryPhoneNumber())) {
            boolean exists = farmerRepository.existsByPrimaryPhoneNumberAndStatusIn(
                farmerUpdateDto.getContactInformation().getPrimaryPhoneNumber(),
                List.of(FarmerStatus.ACTIVE, FarmerStatus.PENDING_APPROVAL)
            );
            if (exists) {
                log.warn("Attempt to update farmer {} with duplicate primary phone number: {}", farmerIdValue, farmerUpdateDto.getContactInformation().getPrimaryPhoneNumber());
                throw new DuplicateFarmerResourceException("Primary phone number", farmerUpdateDto.getContactInformation().getPrimaryPhoneNumber());
            }
        }


        // Apply updates to the domain object. Sensitive fields need careful handling for encryption.
        // The Farmer aggregate's methods should handle this.
        // e.g., farmer.updateProfileDetails(farmerUpdateDto, encryptionService);
        // farmer.updateContactInformation(farmerUpdateDto.getContactInformation(), encryptionService);
        // For now, directly map and apply (simplified):
        farmerDtoMapper.updateFarmerFromDto(farmerUpdateDto, farmer, encryptionService); // This mapper method needs to handle encryption
        farmer.getAuditInfo().setLastUpdatedBy(submittingUserId);
        farmer.getAuditInfo().setLastUpdatedDate(LocalDateTime.now());


        boolean criticalChange = hasCriticalChanges(changedFields); // Placeholder: check app.approval.critical-fields config

        if (criticalChange && !isAdminUpdate) {
            log.info("Critical fields changed for farmer {}. Submitting for approval.", farmerIdValue);
            // farmer.changeStatus(FarmerStatus.PENDING_APPROVAL, "Critical fields updated, pending approval.", submittingUserId);
            // The above status change should be part of the approval submission logic or handled by it.
            farmerApprovalApplicationService.submitForApproval(farmer, changedFields, submittingUserId);
            // eventPublisher.publishEvent(new CriticalFieldChangeSubmittedForApprovalEvent(...));
        } else {
            if (criticalChange && isAdminUpdate) { // REQ-FRM-024
                log.info("Admin {} directly modified critical fields for farmer {}.", submittingUserId, farmerIdValue);
                farmerApprovalApplicationService.logAdminModification(farmer, changedFields, submittingUserId, "Admin direct modification.");
            }
            // If not critical, or admin update, save directly
        }
        Farmer updatedFarmer = farmerRepository.save(farmer);
        return farmerDtoMapper.toFarmerDetailsDto(updatedFarmer);
    }


    @Transactional(readOnly = true)
    public Page<FarmerDetailsDto> searchFarmers(FarmerSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching farmers with criteria: {} and pageable: {}", criteria, pageable);
        Page<Farmer> farmerPage = farmerRepository.search(criteria, pageable);
        return farmerPage.map(farmerDtoMapper::toFarmerDetailsDto); // Or a summary DTO
    }

    @Transactional
    public FarmerDetailsDto updateFarmerStatus(UUID farmerIdValue, FarmerStatus newStatus, String reason, String updatingUserId) {
        log.info("Updating status for farmer ID: {} to {} by user: {}. Reason: {}", farmerIdValue, newStatus, updatingUserId, reason);
        FarmerId farmerId = FarmerId.of(farmerIdValue);
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new FarmerNotFoundException(farmerIdValue));

        farmer.changeStatus(newStatus, reason, updatingUserId); // Domain method handles logic
        Farmer updatedFarmer = farmerRepository.save(farmer);

        // REQ-FRM-022: Approval history for status change is part of ApprovalHistoryEntry
        // This could be logged via farmer.addApprovalEntry(...) or by FarmerApprovalApplicationService
        // For simplicity, let's assume changeStatus also adds an approval history entry if applicable.

        return farmerDtoMapper.toFarmerDetailsDto(updatedFarmer);
    }

    @Transactional
    public FarmerDetailsDto recordConsent(UUID farmerIdValue, boolean consentGiven, String purpose, String versionId, String recordingUserId) {
        log.info("Recording consent for farmer ID: {} by user: {}. Purpose: {}, Version: {}, Given: {}",
                farmerIdValue, recordingUserId, purpose, versionId, consentGiven);
        FarmerId farmerId = FarmerId.of(farmerIdValue);
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new FarmerNotFoundException(farmerIdValue));

        // Assuming Farmer domain object has a method to record/update consent
        farmer.recordConsent(consentGiven, purpose, versionId, recordingUserId, LocalDateTime.now());
        Farmer updatedFarmer = farmerRepository.save(farmer);
        return farmerDtoMapper.toFarmerDetailsDto(updatedFarmer);
    }

    // Placeholder methods, actual implementation would be more complex
    private Map<String, Object> identifyChangedFields(Farmer farmer, FarmerUpdateDto dto) {
        // Compare farmer state with dto and return a map of changed fields and their new values.
        // This is crucial for the approval workflow.
        // Example: if (dto.getFirstName() != null && !dto.getFirstName().equals(farmer.getFullName().getFirstName())) { changes.put("firstName", dto.getFirstName()); }
        return Map.of("placeholder_change", "new_value"); // Replace with actual logic
    }

    private boolean hasCriticalChanges(Map<String, Object> changedFields) {
        // Logic to check if any of the changedFields keys are in the configured list of critical fields.
        // List<String> criticalFieldsList = List.of("nationalIdNumber", "primaryPhoneNumber", "bankAccountNumber"); // Example
        // return changedFields.keySet().stream().anyMatch(criticalFieldsList::contains);
        return !changedFields.isEmpty() && changedFields.containsKey("placeholder_change"); // Replace with actual logic
    }


    // This method would be called by a listener or the approval orchestrator upon approval outcome.
    @Transactional
    public void handleApprovalOutcome(UUID farmerIdValue, UUID approvalRequestId, FarmerStatus status, String comments, String approvedRejectedByUserId) {
        log.info("Handling approval outcome for farmer ID: {}, approval request ID: {}. Status: {}, User: {}, Comments: {}",
                farmerIdValue, approvalRequestId, status, approvedRejectedByUserId, comments);
        FarmerId farmerId = FarmerId.of(farmerIdValue);
        Farmer farmer = farmerRepository.findById(farmerId)
                .orElseThrow(() -> new FarmerNotFoundException(farmerIdValue));

        // Update farmer status based on approval outcome
        // Add to approval history (this might be duplicative if FarmerApprovalApplicationService already does this)
        // The FarmerApprovalApplicationService should be the primary handler for this.
        // This method might just update the farmer's core status.
        String reason = "Approval outcome: " + status + ". Comments: " + comments;
        farmer.changeStatus(status, reason, approvedRejectedByUserId); // Or specific status like FarmerStatus.ACTIVE if outcome is APPROVED

        if (status == FarmerStatus.APPROVED || status == FarmerStatus.ACTIVE) {
            // eventPublisher.publishEvent(new FarmerRegisteredEvent(farmer.getFarmerId(), LocalDateTime.now()));
            log.info("Farmer {} registration/update approved.", farmer.getFarmerId().getValue());
        } else {
            log.warn("Farmer {} registration/update rejected or other status: {}.", farmer.getFarmerId().getValue(), status);
        }
        farmerRepository.save(farmer);
    }
}