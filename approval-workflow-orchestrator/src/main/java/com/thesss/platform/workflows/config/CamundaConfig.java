package com.thesss.platform.workflows.config;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.CamundaHistoryLevelAutoHandlingConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.Ordering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class CamundaConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(CamundaConfig.class);

    // Example of how you might add a ProcessEnginePlugin or customize configuration.
    // Most common configurations are handled by Spring Boot AutoConfiguration for Camunda.
    // @Bean
    // public SpringProcessEngineConfiguration processEngineConfiguration() {
    //     SpringProcessEngineConfiguration config = new SpringProcessEngineConfiguration();
    //     // Add custom configurations here
    //     // e.g., config.setJobExecutorActivate(true);
    //     // e.g., config.setHistory(ProcessEngineConfiguration.HISTORY_FULL);
    //     LOGGER.info("Customizing Camunda Process Engine Configuration.");
    //     return config;
    // }

    /**
     * This is an example of a configuration that runs after Camunda's history level auto-detection.
     * You can use similar configurations to customize the engine after certain auto-configurations have run.
     */
    @Configuration
    @Order(Ordering.DEFAULT_ORDER + 1)
    static class CustomCamundaHistoryConfiguration extends CamundaHistoryLevelAutoHandlingConfiguration {
        @Override
        public void configure(SpringProcessEngineConfiguration configuration) {
            super.configure(configuration);
            LOGGER.info("Applying custom configurations after Camunda history level auto-handling. History Level: {}", configuration.getHistory());
            // For example, you could set a default serialization format or add plugins:
            // configuration.setDefaultSerializationFormat(Variables.SerializationDataFormats.JSON.getName());
            // List<ProcessEnginePlugin> processEnginePlugins = new ArrayList<>(configuration.getProcessEnginePlugins());
            // processEnginePlugins.add(new MyCustomProcessEnginePlugin());
            // configuration.setProcessEnginePlugins(processEnginePlugins);
        }
    }

    // If you need to integrate a custom IdentityProvider:
    // @Bean
    // public ProcessEnginePlugin customIdentityProviderPlugin(MyCustomIdentityProviderFactory myCustomIdentityProviderFactory) {
    //     return new ProcessEnginePlugin() {
    //         @Override
    //         public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    //             processEngineConfiguration.setIdentityProviderSessionFactory(myCustomIdentityProviderFactory);
    //             LOGGER.info("Registered custom IdentityProviderSessionFactory.");
    //         }
    //         @Override
    //         public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {}
    //         @Override
    //         public void postProcessEngineBuild(ProcessEngine processEngine) {}
    //     };
    // }
}