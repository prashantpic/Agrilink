package com.thesss.platform.land.api.controller;

import com.thesss.platform.land.api.dto.request.*;
import com.thesss.platform.land.api.dto.response.FarmLandRecordResponse;
import com.thesss.platform.land.api.dto.response.PagedResponse;
import com.thesss.platform.land.api.mapper.FarmLandRecordApiMapper;
import com.thesss.platform.land.application.port.in.*;
import com.thesss.platform.land.application.dto.command.*;
import com.thesss.platform.land.application.dto.query.FarmLandRecordQuery;
import com.thesss.platform.land.domain.model.FarmerId;
import com.thesss.platform.land.domain.model.LandRecordId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/land-records")
@RequiredArgsConstructor // REQ-2-001, REQ-2-003, REQ-2-004 to REQ-2-017, REQ-2-019 to REQ-2-021
public class FarmLandRecordController {

    private final CreateFarmLandRecordUseCase createFarmLandRecordUseCase;
    private final UpdateFarmLandRecordUseCase updateFarmLandRecordUseCase;
    private final GetFarmLandRecordUseCase getFarmLandRecordUseCase;
    private final LinkGeospatialDataUseCase linkGeospatialDataUseCase;
    private final ManageLandRecordStatusUseCase manageLandRecordStatusUseCase;
    private final FarmLandRecordApiMapper apiMapper;

    @PostMapping // REQ-2-001
    public ResponseEntity<UUID> createFarmLandRecord(@Valid @RequestBody CreateFarmLandRecordRequest request) {
        CreateFarmLandRecordCommand command = apiMapper.toCreateFarmLandRecordCommand(request);
        LandRecordId landRecordId = createFarmLandRecordUseCase.createFarmLandRecord(command);
        return new ResponseEntity<>(landRecordId.getValue(), HttpStatus.CREATED);
    }

    @PutMapping("/{landRecordId}") // REQ-2-001
    public ResponseEntity<Void> updateFarmLandRecord(
            @PathVariable UUID landRecordId,
            @Valid @RequestBody UpdateFarmLandRecordRequest request) {
        UpdateFarmLandRecordCommand command = apiMapper.toUpdateFarmLandRecordCommand(request);
        updateFarmLandRecordUseCase.updateFarmLandRecord(new LandRecordId(landRecordId), command);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{landRecordId}") // REQ-2-001
    public ResponseEntity<FarmLandRecordResponse> getFarmLandRecord(@PathVariable UUID landRecordId) {
        FarmLandRecordQuery query = getFarmLandRecordUseCase.getFarmLandRecordById(new LandRecordId(landRecordId));
        FarmLandRecordResponse response = apiMapper.toFarmLandRecordResponse(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping // REQ-2-001
    public ResponseEntity<PagedResponse<FarmLandRecordResponse>> getFarmLandRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<FarmLandRecordQuery> queryPage = getFarmLandRecordUseCase.getAllFarmLandRecords(pageable);
        Page<FarmLandRecordResponse> responsePage = queryPage.map(apiMapper::toFarmLandRecordResponse);
        PagedResponse<FarmLandRecordResponse> pagedResponse = apiMapper.toPagedResponse(responsePage);
        return ResponseEntity.ok(pagedResponse);
    }

    @GetMapping("/farmer/{farmerId}") // REQ-2-003, REQ-2-017
    public ResponseEntity<PagedResponse<FarmLandRecordResponse>> getFarmLandRecordsByFarmer(
            @PathVariable UUID farmerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<FarmLandRecordQuery> queryPage = getFarmLandRecordUseCase.getFarmLandRecordsByFarmerId(new FarmerId(farmerId), pageable);
        Page<FarmLandRecordResponse> responsePage = queryPage.map(apiMapper::toFarmLandRecordResponse);
        PagedResponse<FarmLandRecordResponse> pagedResponse = apiMapper.toPagedResponse(responsePage);
        return ResponseEntity.ok(pagedResponse);
    }

    @PutMapping("/{landRecordId}/boundary") // REQ-2-020, REQ-1.3-003
    public ResponseEntity<Void> defineLandBoundary(
            @PathVariable UUID landRecordId,
            @Valid @RequestBody DefineBoundaryRequest request) {
        DefineLandBoundaryCommand command = apiMapper.toDefineLandBoundaryCommand(request);
        linkGeospatialDataUseCase.defineLandBoundary(new LandRecordId(landRecordId), command);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{landRecordId}/pois") // REQ-2-020, REQ-1.3-003
    public ResponseEntity<Void> addPointOfInterest(
            @PathVariable UUID landRecordId,
            @Valid @RequestBody AddPointOfInterestRequest request) {
        AddPointOfInterestCommand command = apiMapper.toAddPointOfInterestCommand(request);
        linkGeospatialDataUseCase.addPointOfInterest(new LandRecordId(landRecordId), command);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{landRecordId}/status") // REQ-2-019, REQ-2-021
    public ResponseEntity<Void> updateLandRecordStatus(
            @PathVariable UUID landRecordId,
            @Valid @RequestBody UpdateLandRecordStatusRequest request) {
        manageLandRecordStatusUseCase.updateLandRecordStatus(
                new LandRecordId(landRecordId),
                request.getStatus(),
                request.getReason());
        return ResponseEntity.noContent().build();
    }
}