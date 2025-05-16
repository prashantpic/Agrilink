package com.thesss.platform.crop.interfaces.rest;

import com.thesss.platform.crop.application.dtos.*;
import com.thesss.platform.crop.application.services.CropCycleApplicationService;
import com.thesss.platform.crop.domain.exceptions.ValidationException;
import com.thesss.platform.crop.interfaces.rest.dtos.*;
import com.thesss.platform.crop.interfaces.rest.mappers.CropCycleRestMapper;
import com.thesss.platform.crop.interfaces.rest.mappers.FarmingActivityRestMapper;
import com.thesss.platform.crop.interfaces.rest.mappers.InputUsageRestMapper;
import com.thesss.platform.crop.interfaces.rest.mappers.MarketSaleRestMapper;
import com.thesss.platform.crop.domain.model.statemachine.CropCycleEvent;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/crop-cycles")
public class CropCycleController {

    private static final Logger logger = LoggerFactory.getLogger(CropCycleController.class);

    private final CropCycleApplicationService cropCycleApplicationService;
    private final CropCycleRestMapper cropCycleRestMapper;
    private final FarmingActivityRestMapper farmingActivityRestMapper;
    private final InputUsageRestMapper inputUsageRestMapper;
    private final MarketSaleRestMapper marketSaleRestMapper;

    public CropCycleController(CropCycleApplicationService cropCycleApplicationService,
                               CropCycleRestMapper cropCycleRestMapper,
                               FarmingActivityRestMapper farmingActivityRestMapper,
                               InputUsageRestMapper inputUsageRestMapper,
                               MarketSaleRestMapper marketSaleRestMapper) {
        this.cropCycleApplicationService = cropCycleApplicationService;
        this.cropCycleRestMapper = cropCycleRestMapper;
        this.farmingActivityRestMapper = farmingActivityRestMapper;
        this.inputUsageRestMapper = inputUsageRestMapper;
        this.marketSaleRestMapper = marketSaleRestMapper;
    }

    @PostMapping
    public ResponseEntity<CropCycleResponse> createCropCycle(@Valid @RequestBody CreateCropCycleRequest request) {
        logger.info("Received request to create crop cycle: {}", request);
        CreateCropCycleCommand command = cropCycleRestMapper.toCommand(request);
        CropCycleDto createdCropCycle = cropCycleApplicationService.createCropCycle(command);
        CropCycleResponse response = cropCycleRestMapper.toResponse(createdCropCycle);
        logger.info("Crop cycle created successfully with ID: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CropCycleResponse> getCropCycleById(@PathVariable UUID id) {
        logger.info("Received request to get crop cycle by ID: {}", id);
        CropCycleDto cropCycleDto = cropCycleApplicationService.getCropCycleById(id);
        CropCycleResponse response = cropCycleRestMapper.toResponse(cropCycleDto);
        logger.info("Returning crop cycle with ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/core-info")
    public ResponseEntity<CropCycleResponse> updateCropCycleCoreInfo(@PathVariable UUID id, @Valid @RequestBody UpdateCropCycleCoreInfoRequest request) {
        logger.info("Received request to update core info for crop cycle ID: {}", id);
        UpdateCropCycleCoreInfoCommand command = cropCycleRestMapper.toCommand(request);
        CropCycleDto updatedCropCycle = cropCycleApplicationService.updateCropCycleCoreInfo(id, command);
        CropCycleResponse response = cropCycleRestMapper.toResponse(updatedCropCycle);
        logger.info("Core info updated successfully for crop cycle ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{cropCycleId}/activities")
    public ResponseEntity<FarmingActivityResponse> addFarmingActivity(@PathVariable UUID cropCycleId, @Valid @RequestBody AddFarmingActivityRequest request) {
        logger.info("Received request to add farming activity to crop cycle ID: {}", cropCycleId);
        AddFarmingActivityCommand command = farmingActivityRestMapper.toCommand(request);
        FarmingActivityDto createdActivity = cropCycleApplicationService.addFarmingActivity(cropCycleId, command);
        FarmingActivityResponse response = farmingActivityRestMapper.toResponse(createdActivity);
        logger.info("Farming activity added successfully with ID: {} to crop cycle ID: {}", response.getId(), cropCycleId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{cropCycleId}/activities/{activityId}/inputs")
    public ResponseEntity<InputUsageResponse> logInputUsageForActivity(@PathVariable UUID cropCycleId, @PathVariable UUID activityId, @Valid @RequestBody LogInputUsageRequest request) {
        logger.info("Received request to log input usage for activity ID: {} in crop cycle ID: {}", activityId, cropCycleId);
        LogInputCommand command = inputUsageRestMapper.toCommand(request);
        InputUsageDto loggedInput = cropCycleApplicationService.logInputUsage(cropCycleId, activityId, command);
        InputUsageResponse response = inputUsageRestMapper.toResponse(loggedInput);
        logger.info("Input usage logged successfully with ID: {} for activity ID: {}", response.getId(), activityId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{cropCycleId}/harvest")
    public ResponseEntity<CropCycleResponse> recordHarvest(@PathVariable UUID cropCycleId, @Valid @RequestBody RecordHarvestRequest request) {
        logger.info("Received request to record harvest for crop cycle ID: {}", cropCycleId);
        RecordHarvestCommand command = cropCycleRestMapper.toCommand(request);
        CropCycleDto updatedCropCycle = cropCycleApplicationService.recordHarvest(cropCycleId, command);
        CropCycleResponse response = cropCycleRestMapper.toResponse(updatedCropCycle);
        logger.info("Harvest recorded successfully for crop cycle ID: {}", response.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{cropCycleId}/sales")
    public ResponseEntity<MarketSaleResponse> addMarketSale(@PathVariable UUID cropCycleId, @Valid @RequestBody AddMarketSaleRequest request) {
        logger.info("Received request to add market sale for crop cycle ID: {}", cropCycleId);
        AddMarketSaleCommand command = marketSaleRestMapper.toCommand(request);
        MarketSaleDto addedSale = cropCycleApplicationService.addMarketSale(cropCycleId, command);
        MarketSaleResponse response = marketSaleRestMapper.toResponse(addedSale);
        logger.info("Market sale added successfully with ID: {} for crop cycle ID: {}", response.getId(), cropCycleId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{cropCycleId}/events")
    public ResponseEntity<CropCycleResponse> processCropCycleEvent(@PathVariable UUID cropCycleId, @Valid @RequestBody ProcessCropCycleEventRequest request) {
        logger.info("Received request to process event '{}' for crop cycle ID: {}", request.getEvent(), cropCycleId);
        CropCycleEvent event;
        try {
            event = CropCycleEvent.valueOf(request.getEvent().toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid event string received: {}", request.getEvent());
            throw new ValidationException("Invalid crop cycle event: " + request.getEvent());
        }
        Map<String, Object> context = request.getContext();
        CropCycleDto updatedCropCycle = cropCycleApplicationService.processCropCycleEvent(cropCycleId, event, context);
        CropCycleResponse response = cropCycleRestMapper.toResponse(updatedCropCycle);
        logger.info("Event '{}' processed successfully for crop cycle ID: {}. New status: {}", event, response.getId(), response.getStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<CropCycleResponse>> getCropCyclesByFarmer(@PathVariable UUID farmerId) {
        logger.info("Received request to get crop cycles for farmer ID: {}", farmerId);
        List<CropCycleDto> cropCycleDtos = cropCycleApplicationService.getCropCyclesByFarmer(farmerId);
        List<CropCycleResponse> responses = cropCycleDtos.stream()
                .map(cropCycleRestMapper::toResponse)
                .collect(Collectors.toList());
        logger.info("Returning {} crop cycles for farmer ID: {}", responses.size(), farmerId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/land/{landRecordId}")
    public ResponseEntity<List<CropCycleResponse>> getCropCyclesByLand(@PathVariable UUID landRecordId) {
        logger.info("Received request to get crop cycles for land record ID: {}", landRecordId);
        List<CropCycleDto> cropCycleDtos = cropCycleApplicationService.getCropCyclesByLand(landRecordId);
        List<CropCycleResponse> responses = cropCycleDtos.stream()
                .map(cropCycleRestMapper::toResponse)
                .collect(Collectors.toList());
        logger.info("Returning {} crop cycles for land record ID: {}", responses.size(), landRecordId);
        return ResponseEntity.ok(responses);
    }
}