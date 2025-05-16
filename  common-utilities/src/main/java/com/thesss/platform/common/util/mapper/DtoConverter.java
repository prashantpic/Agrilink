package com.thesss.platform.common.util.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DtoConverter {

    private final ModelMapper modelMapper;

    public DtoConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Converts an entity object to a DTO of the specified class.
     *
     * @param entity      The source entity object.
     * @param dtoClass    The class of the target DTO.
     * @param <D>         The type of the DTO.
     * @param <E>         The type of the Entity.
     * @return The converted DTO, or null if the entity is null.
     */
    public <D, E> D convertToDto(E entity, Class<D> dtoClass) {
        if (entity == null) {
            return null;
        }
        return modelMapper.map(entity, dtoClass);
    }

    /**
     * Converts a DTO object to an entity of the specified class.
     *
     * @param dto         The source DTO object.
     * @param entityClass The class of the target entity.
     * @param <E>         The type of the Entity.
     * @param <D>         The type of the DTO.
     * @return The converted entity, or null if the DTO is null.
     */
    public <E, D> E convertToEntity(D dto, Class<E> entityClass) {
        if (dto == null) {
            return null;
        }
        return modelMapper.map(dto, entityClass);
    }

    /**
     * Converts a list of source objects to a list of target objects.
     *
     * @param sourceList  The list of source objects.
     * @param targetClass The class of the target objects.
     * @param <S>         The type of the source objects.
     * @param <D>         The type of the target objects.
     * @return A list of converted target objects, or an empty list if the source list is null or empty.
     */
    public <S, D> List<D> mapToList(List<S> sourceList, Class<D> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceList.stream()
                .map(element -> modelMapper.map(element, targetClass))
                .collect(Collectors.toList());
    }
}