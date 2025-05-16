package com.thesss.platform.crop.application.services;

import com.thesss.platform.crop.application.dtos.*;
import com.thesss.platform.crop.application.mappers.CropCycleMapper;
import com.thesss.platform.crop.application.mappers.FarmingActivityMapper;
import com.thesss.platform.crop.application.mappers.InputUsageMapper;
import com.thesss.platform.crop.application.mappers.MarketSaleMapper;
import com.thesss.platform.crop.application.ports.outgoing.FarmerServicePort;
import com.thesss.platform.crop.application.ports.outgoing.LandServicePort;
import com.thesss.platform.crop.application.ports.outgoing.MasterDataServicePort;
import com.thesss.platform.crop.domain.model.*;
import com.thesss.platform.crop.domain.model.statemachine.CropCycleEvent;
import com.thesss.platform.crop.domain.model.statemachine.CropCycleState;
import com.thesss.platform.crop.domain.repositories.CropCycleRepository;
import com.thesss.platform.crop.domain.services.HarvestDatePredictionService;
import com.thesss.platform.crop.domain.services.YieldCalculationService;
import com.thesss.platform.crop.domain.exceptions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CropCycleApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(CropCycleApplicationService.class);

    private final CropCycleRepository cropCycleRepository;
    private final CropCycleMapper cropCycleMapper;
    private final FarmingActivityMapper activityMapper;
    private final InputUsageMapper inputMapper;
    private final MarketSaleMapper marketSaleMapper;
    private final FarmerServicePort farmerServicePort;
    private final LandServicePort landServicePort;
    private final MasterDataServicePort masterDataServicePort;
    private final HarvestDatePredictionService harvestDatePredictionService;
    private final YieldCalculationService yieldCalculationService;
    private final StateMachineService<CropCycleState, CropCycleEvent> stateMachineService;


    public CropCycleApplicationService(CropCycleRepository cropCycleRepository,
                                       CropCycleMapper cropCycleMapper,
                                       FarmingActivityMapper activityMapper,
                                       InputUsageMapper inputMapper,
                                       MarketSaleMapper marketSaleMapper,
                                       FarmerServicePort farmerServicePort,
                                       LandServicePort landServicePort,
                                       MasterDataServicePort masterDataServicePort,
                                       HarvestDatePredictionService harvestDatePredictionService,
                                       YieldCalculationService yieldCalculationService,
                                       StateMachineService<CropCycleState, CropCycleEvent> stateMachineService) {
        this.cropCycleRepository = cropCycleRepository;
        this.cropCycleMapper = cropCycleMapper;
        this.activityMapper = activityMapper;
        this.inputMapper = inputMapper;
        this.marketSaleMapper = marketSaleMapper;
        this.farmerServicePort = farmerServicePort;
        this.landServicePort = landServicePort;
        this.masterDataServicePort = masterDataServicePort;
        this.harvestDatePredictionService = harvestDatePredictionService;
        this.yieldCalculationService = yieldCalculationService;
        this.stateMachineService = stateMachineService;
    }

    public CropCycleDto createCropCycle(CreateCropCycleCommand command) {
        logger.info("Creating crop cycle for farmer: {}, land: {}", command.getFarmerId(), command.getLandRecordId());
        validateFarmerAndLand(command.getFarmerId(), command.getLandRecordId());

        LocalDate expectedHarvestDate = harvestDatePredictionService.predictExpectedHarvestDate(
            command.getCropNameMasterId(),
            command.getCropVarietyMasterIdOrText(),
            command.getActualSowingDate(),
            masterDataServicePort
        );

        CropCycle newCropCycle = cropCycleMapper.toDomain(command);
        // Update the immutable SowingInformation with the predicted date
        SowingInformation updatedSowingInfo = newCropCycle.getSowingInformation().updateExpectedHarvestDate(expectedHarvestDate);
        newCropCycle.updateCoreInfo(null, updatedSowingInfo, null, null); // Only update sowing info here

        // Initial status is set by the mapper to PLANNED or determined by StateMachine config
        // For explicit control, ensure the statusInfo is set correctly.
        if (newCropCycle.getStatusInfo() == null || newCropCycle.getStatusInfo().getStatusMasterId() == null) {
            newCropCycle.updateStatus(CropCycleState.PLANNED.name(), null);
        }

        newCropCycle = cropCycleRepository.save(newCropCycle);
        logger.info("Saved new crop cycle with ID: {} and Business ID: {}", newCropCycle.getId(), newCropCycle.getCropCycleId().getValue());

        // Initialize state machine
        StateMachine<CropCycleState, CropCycleEvent> sm = stateMachineService.acquireStateMachine(
            newCropCycle.getCropCycleId().getValue().toString(), false); // doNotSave=false to let persister handle

        if (sm.getState() == null || !sm.getState().getId().equals(CropCycleState.PLANNED)) {
            // This might occur if the SM was acquired and somehow not in initial state
            // Or if the state in the domain object was different.
            // Typically, PersistStateMachineHandler should restore or set initial state.
            logger.warn("State machine for {} acquired with state {} instead of initial PLANNED. Resetting.", sm.getId(), sm.getState() != null ? sm.getState().getId() : "null");
            // Reset logic might be needed if state isn't PLANNED
        }
        sm.startReactively().block(); // Start SM if not already started (reactive start)

        return mapCropCycleToDto(newCropCycle);
    }

    public CropCycleDto getCropCycleById(UUID id) {
        logger.debug("Fetching crop cycle by ID: {}", id);
        CropCycle cropCycle = findCropCycleOrThrow(id);
        return mapCropCycleToDto(cropCycle);
    }

    public CropCycleDto updateCropCycleCoreInfo(UUID id, UpdateCropCycleCoreInfoCommand command) {
        logger.info("Updating core info for crop cycle ID: {}", id);
        CropCycle cropCycle = findCropCycleOrThrow(id);

        // The UpdateCropCycleCoreInfoCommand might not have farmerId/landRecordId as per REST DTO
        // If the command DTO could have them, validation would be here:
        // if (command.getFarmerId() != null) validateFarmerAndLand(command.getFarmerId(), null);
        // if (command.getLandRecordId() != null) validateFarmerAndLand(null, command.getLandRecordId());

        // Manually create new Value Objects for update, as they are immutable
        // MapStruct @MappingTarget for VOs is tricky.
        CropDetails updatedCropDetails = cropCycle.getCropDetails();
        if (command.getCropNameMasterId() != null || command.getCropVarietyMasterIdOrText() != null || command.getSeasonMasterId() != null || command.getCultivationYear() != null) {
            updatedCropDetails = new CropDetails(
                command.getCropNameMasterId() != null ? command.getCropNameMasterId() : cropCycle.getCropDetails().getCropNameMasterId(),
                command.getCropVarietyMasterIdOrText() != null ? command.getCropVarietyMasterIdOrText() : cropCycle.getCropDetails().getCropVarietyMasterIdOrText(),
                command.getSeasonMasterId() != null ? command.getSeasonMasterId() : cropCycle.getCropDetails().getSeasonMasterId(),
                command.getCultivationYear() != null ? command.getCultivationYear() : cropCycle.getCropDetails().getCultivationYear()
            );
        }

        SowingInformation updatedSowingInfo = cropCycle.getSowingInformation();
        if (command.getPlannedSowingDate() != null || command.getActualSowingDate() != null || command.getExpectedHarvestDate() != null || command.getSeedingRateValue() != null || command.getSeedingRateUnitMasterId() != null || command.getSeedSourceMasterIdOrText() != null) {
            updatedSowingInfo = new SowingInformation(
                command.getPlannedSowingDate() != null ? command.getPlannedSowingDate() : cropCycle.getSowingInformation().getPlannedSowingDate(),
                command.getActualSowingDate() != null ? command.getActualSowingDate() : cropCycle.getSowingInformation().getActualSowingDate(),
                command.getExpectedHarvestDate() != null ? command.getExpectedHarvestDate() : cropCycle.getSowingInformation().getExpectedHarvestDate(),
                command.getSeedingRateValue() != null ? command.getSeedingRateValue() : cropCycle.getSowingInformation().getSeedingRateValue(),
                command.getSeedingRateUnitMasterId() != null ? command.getSeedingRateUnitMasterId() : cropCycle.getSowingInformation().getSeedingRateUnitMasterId(),
                command.getSeedSourceMasterIdOrText() != null ? command.getSeedSourceMasterIdOrText() : cropCycle.getSowingInformation().getSeedSourceMasterIdOrText()
            );
        }

        CultivationArea updatedCultivatedArea = cropCycle.getCultivatedArea();
        if (command.getCultivatedAreaValue() != null || command.getCultivatedAreaUnitMasterId() != null) {
            updatedCultivatedArea = new CultivationArea(
                command.getCultivatedAreaValue() != null ? command.getCultivatedAreaValue() : cropCycle.getCultivatedArea().getAreaValue(),
                command.getCultivatedAreaUnitMasterId() != null ? command.getCultivatedAreaUnitMasterId() : cropCycle.getCultivatedArea().getAreaUnitMasterId()
            );
        }
        
        String updatedNotes = command.getNotes() != null ? command.getNotes() : cropCycle.getNotes();

        cropCycle.updateCoreInfo(updatedCropDetails, updatedSowingInfo, updatedCultivatedArea, updatedNotes);

        // Re-predict expected harvest date if relevant fields changed
        boolean needsPrediction = (command.getActualSowingDate() != null && !command.getActualSowingDate().equals(cropCycle.getSowingInformation().getActualSowingDate())) ||
                                  (command.getCropNameMasterId() != null && !command.getCropNameMasterId().equals(cropCycle.getCropDetails().getCropNameMasterId())) ||
                                  (command.getCropVarietyMasterIdOrText() != null && !command.getCropVarietyMasterIdOrText().equals(cropCycle.getCropDetails().getCropVarietyMasterIdOrText()));
        
        if (needsPrediction) {
            LocalDate newExpectedHarvestDate = harvestDatePredictionService.predictExpectedHarvestDate(
                cropCycle.getCropDetails().getCropNameMasterId(),
                cropCycle.getCropDetails().getCropVarietyMasterIdOrText(),
                cropCycle.getSowingInformation().getActualSowingDate(),
                masterDataServicePort
            );
            // Update SowingInformation again if predicted date changed
            if (!newExpectedHarvestDate.equals(cropCycle.getSowingInformation().getExpectedHarvestDate())) {
                 SowingInformation finalSowingInfo = cropCycle.getSowingInformation().updateExpectedHarvestDate(newExpectedHarvestDate);
                 cropCycle.updateCoreInfo(null, finalSowingInfo, null, null);
            }
        }

        CropCycle updatedCropCycle = cropCycleRepository.save(cropCycle);
        return mapCropCycleToDto(updatedCropCycle);
    }

    public FarmingActivityDto addFarmingActivity(UUID cropCycleId, AddFarmingActivityCommand command) {
        logger.info("Adding farming activity to crop cycle ID: {}", cropCycleId);
        CropCycle cropCycle = findCropCycleOrThrow(cropCycleId);
        FarmingActivity newActivity = activityMapper.toDomain(command);
        cropCycle.addFarmingActivity(newActivity); // Aggregate manages its children
        cropCycleRepository.save(cropCycle); // Cascade save

        // Find the newly added activity from the saved aggregate to return its DTO with ID
        FarmingActivity savedActivity = cropCycle.getActivities().stream()
            .filter(a -> a.getActivityTypeMasterId().equals(newActivity.getActivityTypeMasterId()) &&
                         a.getActivityDate().equals(newActivity.getActivityDate()) &&
                         ((a.getNotes() == null && newActivity.getNotes() == null) || (a.getNotes() != null && a.getNotes().equals(newActivity.getNotes())))
            ) // This is a fragile way to find it; ideally, the save operation returns the entity with ID
            .reduce((first, second) -> second) // Get the last added if multiple match (e.g., if ID not set yet)
            .orElseThrow(() -> new RuntimeException("Failed to retrieve saved activity after adding."));

        return activityMapper.toDto(savedActivity);
    }

    public InputUsageDto logInputUsage(UUID cropCycleId, UUID activityId, LogInputCommand command) {
        logger.info("Logging input usage for activity ID: {} in crop cycle ID: {}", activityId, cropCycleId);
        CropCycle cropCycle = findCropCycleOrThrow(cropCycleId);
        FarmingActivity activity = cropCycle.getActivities().stream()
            .filter(a -> a.getId().equals(activityId))
            .findFirst()
            .orElseThrow(() -> new ValidationException("Farming activity with ID " + activityId + " not found in crop cycle " + cropCycleId));

        InputUsage newInputUsage = inputMapper.toDomain(command);
        activity.addInputUsage(newInputUsage);
        cropCycleRepository.save(cropCycle);

        InputUsage savedInput = activity.getInputsUsed().stream()
             .filter(iu -> iu.getInputTypeMasterId().equals(newInputUsage.getInputTypeMasterId()) &&
                           iu.getQuantityValue().compareTo(newInputUsage.getQuantityValue()) == 0 &&
                           iu.getQuantityUnitMasterId().equals(newInputUsage.getQuantityUnitMasterId())
             )
             .reduce((first, second) -> second)
             .orElseThrow(() -> new RuntimeException("Failed to retrieve saved input usage after logging."));

        return inputMapper.toDto(savedInput);
    }

    public CropCycleDto recordHarvest(UUID cropCycleId, RecordHarvestCommand command) {
        logger.info("Recording harvest for crop cycle ID: {}", cropCycleId);
        CropCycle cropCycle = findCropCycleOrThrow(cropCycleId);
        HarvestInformation harvestInfo = new HarvestInformation(
            command.getActualHarvestDate(),
            command.getTotalYieldQuantity(),
            command.getTotalYieldUnitMasterId(),
            command.getQualityGradeMasterId()
        );
        cropCycle.recordHarvestDetails(harvestInfo); // Domain method performs validation (REQ-4-009)
        cropCycleRepository.save(cropCycle);

        // Trigger state machine event
        processStateMachineEvent(cropCycle, CropCycleEvent.START_HARVEST, null);
        
        CropCycle updatedCropCycle = findCropCycleOrThrow(cropCycleId); // Reload after SM action
        return mapCropCycleToDto(updatedCropCycle);
    }

    public MarketSaleDto addMarketSale(UUID cropCycleId, AddMarketSaleCommand command) {
        logger.info("Adding market sale to crop cycle ID: {}", cropCycleId);
        CropCycle cropCycle = findCropCycleOrThrow(cropCycleId);
        MarketSale newSale = marketSaleMapper.toDomain(command);
        cropCycle.addMarketSale(newSale);
        cropCycleRepository.save(cropCycle);

        MarketSale savedSale = cropCycle.getMarketSales().stream()
             .filter(ms -> ms.getQuantitySoldValue().compareTo(newSale.getQuantitySoldValue()) == 0 &&
                           ms.getQuantitySoldUnitMasterId().equals(newSale.getQuantitySoldUnitMasterId()) &&
                           ms.getSalePricePerUnit().compareTo(newSale.getSalePricePerUnit()) == 0 &&
                           ms.getSaleDate().equals(newSale.getSaleDate())
             )
             .reduce((first, second) -> second)
             .orElseThrow(() -> new RuntimeException("Failed to retrieve saved market sale after adding."));

        return marketSaleMapper.toDto(savedSale);
    }

    public CropCycleDto processCropCycleEvent(UUID cropCycleId, CropCycleEvent event, Map<String, Object> context) {
        logger.info("Processing event {} for crop cycle ID: {}", event, cropCycleId);
        CropCycle cropCycle = findCropCycleOrThrow(cropCycleId);
        processStateMachineEvent(cropCycle, event, context);
        CropCycle updatedCropCycle = findCropCycleOrThrow(cropCycleId); // Reload after SM action
        return mapCropCycleToDto(updatedCropCycle);
    }

    public List<CropCycleDto> getCropCyclesByFarmer(UUID farmerId) {
        logger.debug("Fetching crop cycles for farmer ID: {}", farmerId);
        List<CropCycle> cropCycles = cropCycleRepository.findByFarmerId(farmerId);
        return cropCycles.stream().map(this::mapCropCycleToDto).collect(Collectors.toList());
    }

    public List<CropCycleDto> getCropCyclesByLand(UUID landRecordId) {
        logger.debug("Fetching crop cycles for land record ID: {}", landRecordId);
        List<CropCycle> cropCycles = cropCycleRepository.findByLandRecordId(landRecordId);
        return cropCycles.stream().map(this::mapCropCycleToDto).collect(Collectors.toList());
    }

    // --- Helper methods ---
    private CropCycle findCropCycleOrThrow(UUID id) {
        return cropCycleRepository.findById(id)
            .orElseThrow(() -> new CropCycleNotFoundException("Crop Cycle with ID " + id + " not found."));
    }

    private void validateFarmerAndLand(UUID farmerId, UUID landRecordId) {
        if (farmerId != null) {
            try {
                if (!farmerServicePort.validateFarmerExists(farmerId)) {
                    throw new ValidationException("Farmer with ID " + farmerId + " does not exist or is not valid.");
                }
            } catch (Exception e) {
                logger.error("Error validating farmer {}: {}", farmerId, e.getMessage());
                throw new ExternalServiceException("Error validating farmer existence with Farmer Service.", e);
            }
        }
        if (landRecordId != null) {
            try {
                if (!landServicePort.validateLandRecordExists(landRecordId)) {
                    throw new ValidationException("Land Record with ID " + landRecordId + " does not exist or is not valid.");
                }
            } catch (Exception e) {
                logger.error("Error validating land record {}: {}", landRecordId, e.getMessage());
                throw new ExternalServiceException("Error validating land record existence with Land Service.", e);
            }
        }
    }

    private CropCycleDto mapCropCycleToDto(CropCycle cropCycle) {
        YieldPerUnitArea yield = null;
        if (cropCycle.getHarvestInformation() != null &&
            cropCycle.getHarvestInformation().getTotalYieldQuantity() != null &&
            cropCycle.getCultivatedArea() != null &&
            cropCycle.getCultivatedArea().getAreaValue() != null &&
            cropCycle.getCultivatedArea().getAreaValue().compareTo(BigDecimal.ZERO) > 0) {
            try {
                yield = yieldCalculationService.calculateYieldPerUnitArea(
                    cropCycle.getHarvestInformation(),
                    cropCycle.getCultivatedArea(),
                    masterDataServicePort
                );
            } catch (CalculationException | MasterDataResolutionException e) {
                logger.warn("Could not calculate yield for crop cycle {}: {}", cropCycle.getId(), e.getMessage());
            }
        }
        return cropCycleMapper.toDto(cropCycle, yield);
    }

    private void processStateMachineEvent(CropCycle cropCycle, CropCycleEvent event, Map<String, Object> contextData) {
        String smId = cropCycle.getCropCycleId().getValue().toString();
        StateMachine<CropCycleState, CropCycleEvent> sm = stateMachineService.acquireStateMachine(smId, false);

        // Ensure state machine is started
        if (!sm.isComplete() && sm.getState() == null) { // Check if it's a new/uninitialized SM
            sm.startReactively().block(); // Start it if not already started
        }

        // Add interceptor to pass CropCycle to actions/guards via headers or extended state
        // This is one way; another is to fetch within action/guard using SM ID
        sm.getStateMachineAccessor().doWithAllRegions(access -> {
            access.addStateMachineInterceptor(new StateMachineInterceptorAdapter<>() {
                @Override
                public Message<CropCycleEvent> preEvent(Message<CropCycleEvent> message, StateMachine<CropCycleState, CropCycleEvent> stateMachine) {
                    // Pass DB ID for actions/guards to fetch the entity
                    stateMachine.getExtendedState().getVariables().put("cropCycleDbId", cropCycle.getId());
                    return message;
                }
                @Override
                public void postStateChange(State<CropCycleState, CropCycleEvent> state, Message<CropCycleEvent> message,
                                            org.springframework.statemachine.transition.Transition<CropCycleState, CropCycleEvent> transition,
                                            StateMachine<CropCycleState, CropCycleEvent> stateMachine,
                                            StateMachine<CropCycleState, CropCycleEvent> rootStateMachine) {
                    // Persist the entity after state change by action
                    // The action itself should modify the cropCycle entity's status
                    // and then it will be saved by the transactional boundary of this service method.
                    // If the action saves, this is fine. If not, save here.
                    // Better: the StateMachinePersist configured should handle saving the context.
                    // The PersistStateMachineHandler's 'write' method is called by framework.
                }
            });
        });
        
        Message<CropCycleEvent> message = MessageBuilder.withPayload(event)
            .setHeader("contextData", contextData == null ? new java.util.HashMap<>() : contextData)
            .setHeader("cropCycleDbId", cropCycle.getId()) // Pass DB ID in message header
            .build();

        boolean accepted = sm.sendEvent(message).blockLast(); // Use reactive sendEvent and block

        if (!accepted) {
            logger.warn("Event {} not accepted for crop cycle {} in state {}", event, smId, sm.getState().getId());
            throw new InvalidCropCycleStateTransitionException("Event " + event + " not accepted for crop cycle in state " + sm.getState().getId());
        }
        logger.info("Event {} processed successfully for crop cycle {}. New state: {}", event, smId, sm.getState().getId());
        // Note: The state machine's runtime persister (configured in StateMachineConfig)
        // should automatically save the state machine context after successful transitions.
        // The CropCycle entity's status field should be updated by a state machine action.
    }
}