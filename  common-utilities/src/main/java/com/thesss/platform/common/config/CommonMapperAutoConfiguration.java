package com.thesss.platform.common.config;

import com.thesss.platform.common.util.mapper.DtoConverter;
import com.thesss.platform.common.util.mapper.ModelMapperConfigurer;
import org.modelmapper.ModelMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonMapperAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ModelMapperConfigurer modelMapperConfigurer() {
        return new ModelMapperConfigurer();
    }

    @Bean
    @ConditionalOnMissingBean
    public ModelMapper modelMapper(ModelMapperConfigurer configurer) {
        return configurer.configure();
    }

    @Bean
    @ConditionalOnMissingBean
    public DtoConverter dtoConverter(ModelMapper modelMapper) {
        return new DtoConverter(modelMapper);
    }
}