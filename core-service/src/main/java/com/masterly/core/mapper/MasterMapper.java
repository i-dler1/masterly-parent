package com.masterly.core.mapper;

import com.masterly.core.dto.MasterDto;
import com.masterly.core.entity.Master;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Маппер для преобразования между сущностью {@link Master} и DTO {@link MasterDto}.
 * Использует MapStruct для автоматической генерации реализации.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MasterMapper {

    /**
     * Преобразовать сущность в DTO.
     *
     * @param master сущность мастера
     * @return DTO с данными мастера
     */
    MasterDto toDto(Master master);

    /**
     * Преобразовать DTO в сущность.
     *
     * @param dto DTO с данными мастера
     * @return сущность мастера
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Master toEntity(MasterDto dto);
}