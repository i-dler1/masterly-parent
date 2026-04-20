package com.masterly.core.mapper;

import com.masterly.core.dto.ServiceEntityDto;
import com.masterly.core.entity.ServiceEntity;
import org.mapstruct.*;

import java.util.List;

/**
 * Маппер для преобразования между сущностью {@link ServiceEntity} и DTO {@link ServiceEntityDto}.
 * Использует MapStruct для автоматической генерации реализации.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServiceEntityMapper {

    /**
     * Преобразовать сущность в DTO.
     *
     * @param serviceEntity сущность услуги
     * @return DTO с данными услуги
     */
    ServiceEntityDto toDto(ServiceEntity serviceEntity);

    /**
     * Преобразовать DTO в сущность.
     *
     * @param requestDto DTO с данными услуги
     * @return сущность услуги
     */
    ServiceEntity toEntity(ServiceEntityDto requestDto);

    List<ServiceEntityDto> toDtoList(List<ServiceEntity> services);
}