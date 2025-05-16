package com.thesss.platform.common.util.mapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelMapperConfigurer {

    private static final Logger log = LoggerFactory.getLogger(ModelMapperConfigurer.class);

    /**
     * Creates and configures a ModelMapper instance with common settings.
     * This method is typically called by the auto-configuration to create the ModelMapper bean.
     *
     * @return A configured ModelMapper instance.
     */
    public ModelMapper configure() {
        ModelMapper modelMapper = new ModelMapper();

        // Configure ModelMapper instance
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT) // Enforce strict mapping
                .setSkipNullEnabled(true)                       // Skip null source properties
                .setFieldMatchingEnabled(true)                  // Match fields directly
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE); // Allow access to private fields

        // Example: Add custom type maps or converters if needed globally
        // modelMapper.addMappings(new PropertyMap<Source, Destination>() {
        //     @Override
        //     protected void configure() {
        //         map().setDestField(source.getSourceField());
        //     }
        // });

        log.info("ModelMapper configured with STRICT matching strategy, skipNullEnabled=true, fieldMatchingEnabled=true, fieldAccessLevel=PRIVATE.");
        return modelMapper;
    }
}