package com.thesss.platform.farmer.controller;

import com.thesss.platform.farmer.controller.dto.*;
import com.thesss.platform.farmer.service.FarmerApprovalApplicationService;
import com.thesss.platform.farmer.service.FarmerProfilePhotoApplicationService;
import com.thesss.platform.farmer.service.FarmerRegistryApplicationService;
import com.thesss.platform.farmer.service.dto.FarmerCreateDto;
import com.thesss.platform.farmer.service.dto.FarmerDetailsDto;
import com.thesss.platform.farmer.service.dto.FarmerUpdateDto;
import com.thesss.platform.farmer.service.mapper.FarmerDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/farmers")
@RequiredArgsConstructor
public class FarmerController {

    private final FarmerRegistryApplicationService farmerRegistryApplicationService;
    private final FarmerProfilePhotoApplicationService farmerProfilePhotoApplicationService;
    private final FarmerApprovalApplicationService farmerApprovalApplicationService;
    private final FarmerDtoMapper farmerDtoMapper; // Assuming this mapper is available

    @PostMapping
    public ResponseEntity<FarmerApiResponse> registerFarmer(@Valid @RequestBody FarmerRegistrationApiRequest request) {
        // Assuming FarmerRegistrationApiRequest and its mapping to FarmerCreateDto exists
        // For now, let's assume a direct or simplified mapping if FarmerRegistrationApiRequest is not yet generated
        FarmerCreateDto createDto = farmerDtoMapper.toFarmerCreateDto(request); // This line will require FarmerRegistrationApiRequest
        FarmerDetailsDto createdFarmer = farmerRegistryApplicationService.registerFarmer(createDto, getCurrentUserId()); // TODO: Implement getCurrentUserId()
        return new ResponseEntity<>(farmerDtoMapper.toFarmerApiResponse(createdFarmer), HttpStatus.CREATED);
    }

    @GetMapping("/{farmerId}")
    public ResponseEntity<FarmerApiResponse> getFarmerById(@PathVariable UUID farmerId) {
        FarmerDetailsDto farmerDetailsDto = farmerRegistryApplicationService.getFarmerById(farmerId, true); // Assuming decryptSensitive=true for direct view
        return ResponseEntity.ok(farmerDtoMapper.toFarmerApiResponse(farmerDetailsDto));
    }

    @PutMapping("/{farmerId}")
    public ResponseEntity<FarmerApiResponse> updateFarmer(@PathVariable UUID farmerId, @Valid @RequestBody FarmerUpdateApiRequest request) {
        // Assuming FarmerUpdateApiRequest and its mapping to FarmerUpdateDto exists
        FarmerUpdateDto updateDto = farmerDtoMapper.toFarmerUpdateDto(request); // This line will require FarmerUpdateApiRequest
        // Determine isAdminUpdate based on user role, placeholder for now
        boolean isAdminUpdate = hasAdminRole(); // TODO: Implement hasAdminRole()
        FarmerDetailsDto updatedFarmer = farmerRegistryApplicationService.updateFarmer(farmerId, updateDto, getCurrentUserId(), isAdminUpdate);
        return ResponseEntity.ok(farmerDtoMapper.toFarmerApiResponse(updatedFarmer));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<FarmerSummaryApiResponse>> searchFarmers(@Valid FarmerSearchApiRequest searchRequest, Pageable pageable) {
        // Assuming FarmerSearchApiRequest and its mapping to FarmerSearchCriteria exists
        // For now, passing searchRequest directly if criteria mapping is complex or criteria object not defined
        // Page<FarmerSummaryDto> results = farmerRegistryApplicationService.searchFarmers(farmerDtoMapper.toFarmerSearchCriteria(searchRequest), pageable);
        // The FarmerSearchApiRequest itself can act as criteria DTO for the service if simple enough
        Page<FarmerDetailsDto> results = farmerRegistryApplicationService.searchFarmers(searchRequest, pageable); // simplified for now
        return ResponseEntity.ok(results.map(farmerDtoMapper::toFarmerSummaryApiResponse));
    }

    @PatchMapping("/{farmerId}/status")
    public ResponseEntity<FarmerApiResponse> updateFarmerStatus(@PathVariable UUID farmerId, @Valid @RequestBody FarmerStatusUpdateApiRequest statusUpdateRequest) {
        FarmerDetailsDto updatedFarmer = farmerRegistryApplicationService.updateFarmerStatus(
                farmerId,
                statusUpdateRequest.getNewStatus(), // Assuming FarmerStatusUpdateApiRequest has getNewStatus() returning FarmerStatus enum or String
                statusUpdateRequest.getReasonForStatusChange(),
                getCurrentUserId()
        );
        return ResponseEntity.ok(farmerDtoMapper.toFarmerApiResponse(updatedFarmer));
    }

    @PostMapping("/{farmerId}/photo")
    public ResponseEntity<ProfilePhotoUploadApiResponse> uploadProfilePhoto(@PathVariable UUID farmerId, @RequestParam("photo") MultipartFile photo) {
        String photoUrl = farmerProfilePhotoApplicationService.uploadProfilePhoto(farmerId, photo, getCurrentUserId());
        ProfilePhotoUploadApiResponse response = new ProfilePhotoUploadApiResponse(photoUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{farmerId}/consent")
    public ResponseEntity<FarmerApiResponse> manageFarmerConsent(@PathVariable UUID farmerId, @Valid @RequestBody ConsentApiRequest consentRequest) {
        FarmerDetailsDto updatedFarmer = farmerRegistryApplicationService.recordConsent(
                farmerId,
                consentRequest.isConsentGiven(),
                consentRequest.getConsentPurpose(),
                consentRequest.getConsentVersionId(),
                getCurrentUserId()
        );
        return ResponseEntity.ok(farmerDtoMapper.toFarmerApiResponse(updatedFarmer));
    }

    @GetMapping("/{farmerId}/approval-history")
    public ResponseEntity<List<ApprovalHistoryApiResponse>> getApprovalHistory(@PathVariable UUID farmerId) {
        List<com.thesss.platform.farmer.service.dto.ApprovalHistoryEntryDto> historyDtos = // Assuming this DTO exists
                farmerApprovalApplicationService.getApprovalHistory(farmerId);
        List<ApprovalHistoryApiResponse> apiResponses = historyDtos.stream()
                .map(farmerDtoMapper::toApprovalHistoryApiResponse) // Assuming this mapping exists
                .collect(Collectors.toList());
        return ResponseEntity.ok(apiResponses);
    }

    // TODO: Implement these helper methods properly, likely by integrating with Spring Security
    private String getCurrentUserId() {
        // Placeholder: In a real app, get this from SecurityContextHolder or a similar mechanism
        return "system-user";
    }

    private boolean hasAdminRole() {
        // Placeholder: Check user roles from SecurityContextHolder
        return false;
    }
}