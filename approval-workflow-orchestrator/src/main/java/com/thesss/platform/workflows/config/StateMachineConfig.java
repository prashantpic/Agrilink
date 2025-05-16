package com.thesss.platform.workflows.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
// import org.springframework.statemachine.config.EnableStateMachineFactory;
// import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
// import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
// import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
// import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

/**
 * Placeholder configuration for Spring State Machine.
 * Uncomment and configure if Spring State Machine is used for simpler, internal state management tasks.
 * The primary cross-service orchestration is handled by Camunda BPMN.
 */
@Configuration
// @EnableStateMachineFactory // If using a specific factory bean name: name = "mySimpleStateMachineFactory"
public class StateMachineConfig /* extends StateMachineConfigurerAdapter<String, String> */ {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateMachineConfig.class);

    public StateMachineConfig() {
        LOGGER.info("StateMachineConfig loaded. If Spring StateMachine is used, further configuration is needed here.");
    }

    // Example State Machine Configuration (if needed)
    /*
    @Override
    public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
        config
            .withConfiguration()
                .autoStartup(true)
                .listener(new StateMachineListener()); // Example listener
    }

    @Override
    public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
        states
            .withStates()
                .initial("S1")
                .state("S2")
                .end("SF");
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
        transitions
            .withExternal()
                .source("S1").target("S2").event("E1")
                .and()
            .withExternal()
                .source("S2").target("SF").event("E2");
    }

    // Define a simple listener if needed
    static class StateMachineListener extends org.springframework.statemachine.listener.StateMachineListenerAdapter<String, String> {
        @Override
        public void stateChanged(org.springframework.statemachine.state.State<String, String> from, org.springframework.statemachine.state.State<String, String> to) {
            LOGGER.info("State changed from {} to {}", from != null ? from.getId() : "null", to.getId());
        }
    }
    */
}