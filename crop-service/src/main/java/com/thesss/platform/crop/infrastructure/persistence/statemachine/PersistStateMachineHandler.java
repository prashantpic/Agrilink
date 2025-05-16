package com.thesss.platform.crop.infrastructure.persistence.statemachine;

import com.thesss.platform.crop.domain.model.CropCycle;
import com.thesss.platform.crop.domain.model.statemachine.CropCycleEvent;
import com.thesss.platform.crop.domain.model.statemachine.CropCycleState;
import com.thesss.platform.crop.infrastructure.persistence.repositories.JpaCropCycleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.persist.DefaultStateMachineRuntimePersister;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class PersistStateMachineHandler {

    private static final Logger log = LoggerFactory.getLogger(PersistStateMachineHandler.class);

    private final JpaCropCycleRepository cropCycleRepository;

    public PersistStateMachineHandler(JpaCropCycleRepository cropCycleRepository) {
        this.cropCycleRepository = cropCycleRepository;
    }

    public class JpaStateMachinePersister implements StateMachinePersist<CropCycleState, CropCycleEvent, String> {

        @Override
        @Transactional // Ensure DB operations are part of a transaction
        public void write(StateMachineContext<CropCycleState, CropCycleEvent> context, String contextObjId) throws Exception {
            log.debug("Persisting state machine context for ID: {}, State: {}", contextObjId, context.getState());
            UUID cropCycleBusinessId = UUID.fromString(contextObjId);
            Optional<CropCycle> cropCycleOptional = cropCycleRepository.findByCropCycleBusinessId(cropCycleBusinessId);

            if (cropCycleOptional.isPresent()) {
                CropCycle cropCycle = cropCycleOptional.get();
                // The primary responsibility of updating the domain entity's status
                // should be within an Action of the state machine transition,
                // ensuring it's part of the same transaction handling the event.
                // This persister's `write` method is more about saving the SM's internal context
                // if it has extended state variables, history, etc.
                // For simple state persistence, the Action is sufficient.
                // If we only persist the state ID here, it might conflict with the Action.
                // Let's assume the Action already updated the CropCycle entity's status.
                // This method could then be used to persist additional SM context if needed.
                // For now, we just log, as the CropCycle entity's status should be the source of truth for its state.
                 log.info("CropCycle {} status is expected to be updated by StateMachine Action to {}.", cropCycle.getId(), context.getState());
                // If storing state machine variables:
                // cropCycle.setStateMachineExtendedStateVariables(context.getExtendedState().getVariables());
                // cropCycleRepository.save(cropCycle);
            } else {
                log.warn("CropCycle with business ID {} not found for state machine persistence.", contextObjId);
                // throw new RuntimeException("CropCycle with business ID " + contextObjId + " not found for state machine persistence.");
            }
        }

        @Override
        @Transactional(readOnly = true)
        public StateMachineContext<CropCycleState, CropCycleEvent> read(String contextObjId) throws Exception {
            log.debug("Reading state machine context for ID: {}", contextObjId);
            UUID cropCycleBusinessId = UUID.fromString(contextObjId);
            Optional<CropCycle> cropCycleOptional = cropCycleRepository.findByCropCycleBusinessId(cropCycleBusinessId);

            if (cropCycleOptional.isPresent()) {
                CropCycle cropCycle = cropCycleOptional.get();
                CropCycleState currentState;
                try {
                    currentState = CropCycleState.valueOf(cropCycle.getStatusInfo().getStatusMasterId());
                } catch (IllegalArgumentException e) {
                    log.error("Invalid state master ID '{}' found for CropCycle business ID {}. Defaulting to PLANNED.",
                              cropCycle.getStatusInfo().getStatusMasterId(), contextObjId);
                    currentState = CropCycleState.PLANNED; // Or handle as an error
                }
                // Recreate a basic context. If extended state variables were persisted, load them here.
                return new DefaultStateMachineContext<>(currentState, null, null, null, null, contextObjId);
            } else {
                log.warn("No persisted state machine context found for CropCycle business ID {}. Returning null.", contextObjId);
                return null; // Or throw an exception if a context is always expected
            }
        }
    }

    @Bean
    public StateMachineRuntimePersister<CropCycleState, CropCycleEvent, String> stateMachineRuntimePersister() {
        return new DefaultStateMachineRuntimePersister<>(new JpaStateMachinePersister());
    }

    @Bean
    public StateMachineListenerAdapter<CropCycleState, CropCycleEvent> stateMachineListener() {
        return new StateMachineListenerAdapter<CropCycleState, CropCycleEvent>() {
            @Override
            public void stateChanged(State<CropCycleState, CropCycleEvent> from, State<CropCycleState, CropCycleEvent> to) {
                log.info("State machine (ID: {}) changed state from {} to {}",
                        (this.stateMachine != null ? this.stateMachine.getId() : "N/A"),
                        (from != null ? from.getId() : "null"),
                        to.getId());
            }

            @Override
            public void eventNotAccepted(Message<CropCycleEvent> event) {
                log.warn("State machine (ID: {}) did not accept event: {}",
                        (this.stateMachine != null ? this.stateMachine.getId() : "N/A"),
                        event.getPayload());
            }

            @Override
            public void transitionEnded(Transition<CropCycleState, CropCycleEvent> transition) {
                 if (transition.getTarget() != null && transition.getSource() != null) {
                     log.debug("State machine (ID: {}) transition ended: {} -> {}",
                             (this.stateMachine != null ? this.stateMachine.getId() : "N/A"),
                             transition.getSource().getId(),
                             transition.getTarget().getId());
                 }
            }
             @Override
             public void stateMachineError(StateMachine<CropCycleState, CropCycleEvent> stateMachine, Exception exception) {
                 log.error("State machine (ID: {}) encountered an error: {}", stateMachine.getId(), exception.getMessage(), exception);
             }

            private StateMachine<CropCycleState, CropCycleEvent> stateMachine;
            @Override
            public void stateMachineStarted(StateMachine<CropCycleState, CropCycleEvent> stateMachine) {
                this.stateMachine = stateMachine;
                log.info("State machine (ID: {}) started.", stateMachine.getId());
            }
        };
    }
}