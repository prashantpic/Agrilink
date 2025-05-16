package com.thesss.platform.crop.config;

import com.thesss.platform.crop.domain.model.statemachine.CropCycleEvent;
import com.thesss.platform.crop.domain.model.statemachine.CropCycleState;
import com.thesss.platform.crop.infrastructure.persistence.statemachine.PersistStateMachineHandler; // Assuming this path
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.service.StateMachineService;
import org.springframework.statemachine.service.DefaultStateMachineService; // Concrete class for StateMachineService

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory // Use factory for multiple SM instances per CropCycle
public class StateMachineConfig extends StateMachineConfigurerAdapter<CropCycleState, CropCycleEvent> {

    private final PersistStateMachineHandler persistStateMachineHandler;
    // Placeholder: You would inject services needed for actions/guards here
    // e.g., private final CropCycleRepository cropCycleRepository;

    public StateMachineConfig(PersistStateMachineHandler persistStateMachineHandler
                              /*, CropCycleRepository cropCycleRepository */) {
        this.persistStateMachineHandler = persistStateMachineHandler;
        // this.cropCycleRepository = cropCycleRepository;
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<CropCycleState, CropCycleEvent> config) throws Exception {
        config
            .withConfiguration()
                .autoStartup(false) // Manage startup explicitly for each crop cycle instance
                .listener(persistStateMachineHandler.stateMachineListener()); // For state change logging/auditing
                // The PersistStateMachineHandler's persister will handle actual persistence
    }

    @Override
    public void configure(StateMachineStateConfigurer<CropCycleState, CropCycleEvent> states) throws Exception {
        states
            .withStates()
                .initial(CropCycleState.PLANNED)
                .states(EnumSet.allOf(CropCycleState.class)) // Define all states from the enum
                .end(CropCycleState.COMPLETED) // Terminal state
                .end(CropCycleState.FAILED);    // Terminal state
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<CropCycleState, CropCycleEvent> transitions) throws Exception {
        transitions
            // PLANNED -> SOWN
            .withExternal()
                .source(CropCycleState.PLANNED).target(CropCycleState.SOWN)
                .event(CropCycleEvent.START_SOWING)
                .guard(actualSowingDateProvidedGuard()) // REQ-4-010: Actual sowing date must be set
                .action(updateCropCycleStatusAction())
            .and()
            // SOWN -> GROWING (assuming COMPLETE_SOWING means SOWN state is done)
            .withExternal()
                .source(CropCycleState.SOWN).target(CropCycleState.GROWING)
                .event(CropCycleEvent.COMPLETE_SOWING) // Or PROGRESS if SOWN -> GROWING is time-based
                .action(updateCropCycleStatusAction())
            .and()
            // GROWING -> FLOWERING (example progression)
            .withExternal()
                .source(CropCycleState.GROWING).target(CropCycleState.FLOWERING)
                .event(CropCycleEvent.PROGRESS) // Generic progress event
                .action(updateCropCycleStatusAction())
            .and()
            // FLOWERING -> FRUITING (example progression)
            .withExternal()
                .source(CropCycleState.FLOWERING).target(CropCycleState.FRUITING)
                .event(CropCycleEvent.PROGRESS)
                .action(updateCropCycleStatusAction())
            .and()
            // FRUITING -> READY_FOR_HARVEST (example progression)
            .withExternal()
                .source(CropCycleState.FRUITING).target(CropCycleState.READY_FOR_HARVEST)
                .event(CropCycleEvent.PROGRESS) // Or a specific event like BECOME_READY_FOR_HARVEST
                .action(updateCropCycleStatusAction())
            .and()
             // READY_FOR_HARVEST -> HARVESTED
            .withExternal()
                .source(CropCycleState.READY_FOR_HARVEST).target(CropCycleState.HARVESTED)
                .event(CropCycleEvent.START_HARVEST) // SDS event, means harvest data recorded
                .guard(harvestDataRecordedGuard()) // Check if harvest details are recorded
                .action(updateCropCycleStatusAction())
            .and()
            // HARVESTED -> COMPLETED
            .withExternal()
                .source(CropCycleState.HARVESTED).target(CropCycleState.COMPLETED)
                .event(CropCycleEvent.COMPLETE_CYCLE)
                .action(updateCropCycleStatusAction())
            .and()
            // Transitions to FAILED (from various non-terminal states)
            .withExternal()
                .source(CropCycleState.PLANNED).target(CropCycleState.FAILED)
                .event(CropCycleEvent.RECORD_FAILURE).action(updateCropCycleStatusAction())
            .and().withExternal()
                .source(CropCycleState.SOWN).target(CropCycleState.FAILED)
                .event(CropCycleEvent.RECORD_FAILURE).action(updateCropCycleStatusAction())
            .and().withExternal()
                .source(CropCycleState.GROWING).target(CropCycleState.FAILED)
                .event(CropCycleEvent.RECORD_FAILURE).action(updateCropCycleStatusAction())
            .and().withExternal()
                .source(CropCycleState.FLOWERING).target(CropCycleState.FAILED)
                .event(CropCycleEvent.RECORD_FAILURE).action(updateCropCycleStatusAction())
            .and().withExternal()
                .source(CropCycleState.FRUITING).target(CropCycleState.FAILED)
                .event(CropCycleEvent.RECORD_FAILURE).action(updateCropCycleStatusAction())
            .and().withExternal()
                .source(CropCycleState.READY_FOR_HARVEST).target(CropCycleState.FAILED)
                .event(CropCycleEvent.RECORD_FAILURE).action(updateCropCycleStatusAction());
            // Note: Not adding FAILED transition from HARVESTED or COMPLETED as those are typically final.
    }

    @Bean
    public StateMachineRuntimePersister<CropCycleState, CropCycleEvent, String> stateMachineRuntimePersister() {
        // This delegates to the PersistStateMachineHandler for actual persistence logic
        return persistStateMachineHandler.stateMachinePersister();
    }

    @Bean
    public StateMachineService<CropCycleState, CropCycleEvent> stateMachineService(
            org.springframework.statemachine.config.StateMachineFactory<CropCycleState, CropCycleEvent> stateMachineFactory,
            StateMachineRuntimePersister<CropCycleState, CropCycleEvent, String> stateMachineRuntimePersister) {
        return new DefaultStateMachineService<>(stateMachineFactory, stateMachineRuntimePersister);
    }

    @Bean
    public Action<CropCycleState, CropCycleEvent> updateCropCycleStatusAction() {
        return context -> {
            // Placeholder: This action should update the CropCycle entity's status in the DB.
            // It needs access to CropCycleRepository and the CropCycle ID (from SM context or headers).
            // The CropCycle ID should be passed via StateMachine.getExtendedState().getVariables()
            // or as a header in the event message.
            System.out.println("Action: Update CropCycle status to " + context.getTarget().getId());
            // Example:
            // UUID cropCycleDbId = (UUID) context.getMessageHeader("cropCycleDbId");
            // if (cropCycleDbId != null) {
            //   CropCycle cycle = cropCycleRepository.findById(cropCycleDbId).orElse(null);
            //   if (cycle != null) {
            //     cycle.updateStatus(context.getTarget().getId().name(), null); // Assuming master data key matches enum name
            //     cropCycleRepository.save(cycle);
            //   }
            // }
        };
    }

    @Bean
    public Guard<CropCycleState, CropCycleEvent> actualSowingDateProvidedGuard() {
        // REQ-4-010: "Ensure actualSowingDate is set before adding activities or recording harvest"
        // This guard applies to transitioning to SOWN if actualSowingDate is a prerequisite.
        return context -> {
            // Placeholder: Check if actualSowingDate is set for the CropCycle.
            // Needs access to CropCycle data, passed via context or message headers.
            System.out.println("Guard: Checking if actualSowingDate is provided.");
            // Example:
            // UUID cropCycleDbId = (UUID) context.getMessageHeader("cropCycleDbId");
            // if (cropCycleDbId != null) {
            //    CropCycle cycle = cropCycleRepository.findById(cropCycleDbId).orElse(null);
            //    return cycle != null && cycle.getSowingInformation() != null && cycle.getSowingInformation().getActualSowingDate() != null;
            // }
            // return false; // Deny if data not available
            return true; // Placeholder
        };
    }

    @Bean
    public Guard<CropCycleState, CropCycleEvent> harvestDataRecordedGuard() {
        return context -> {
            // Placeholder: Check if harvest data (date, yield) is recorded.
            System.out.println("Guard: Checking if harvest data is recorded.");
            // Similar logic to actualSowingDateProvidedGuard, checking HarvestInformation.
            return true; // Placeholder
        };
    }
}