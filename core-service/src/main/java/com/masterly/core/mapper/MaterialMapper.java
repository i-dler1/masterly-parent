package com.masterly.core.mapper;

import com.masterly.core.dto.MaterialDto;
import com.masterly.core.entity.Material;
import org.mapstruct.*;
/**
 * Маппер для преобразования между сущностью {@link Material} и DTO {@link MaterialDto}.
 * Использует MapStruct для автоматической генерации реализации.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MaterialMapper {

    /**
     * Преобразовать сущность в DTO.
     *
     * @param material сущность материала
     * @return DTO с данными материала
     */
    MaterialDto toDto(Material material);

    /**
     * Преобразовать DTO в сущность.
     *
     * @param requestDto DTO с данными материала
     * @return сущность материала
     */
    Material toEntity(MaterialDto requestDto);
}