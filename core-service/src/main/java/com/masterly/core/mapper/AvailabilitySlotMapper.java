package com.masterly.core.mapper;

import com.masterly.core.dto.AvailabilitySlotDto;
import com.masterly.core.entity.AvailabilitySlot;
import com.masterly.core.entity.Master;
import com.masterly.core.entity.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * Маппер для преобразования между сущностью {@link AvailabilitySlot}
 * и DTO {@link AvailabilitySlotDto}.
 * Использует MapStruct для автоматической генерации реализации.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AvailabilitySlotMapper {

    /**
     * Преобразовать сущность {@link AvailabilitySlot} в {@link AvailabilitySlotDto}.
     *
     * @param slot сущность свободных слотов времени
     * @return DTO с данными слотов
     */
    @Mapping(target = "masterId", source = "master.id")
    @Mapping(target = "masterName", source = "master.fullName")
    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "serviceName", source = "service.name")
    AvailabilitySlotDto toDto(AvailabilitySlot slot);


    /**
     * Преобразовать {@link AvailabilitySlotDto} в сущность {@link AvailabilitySlot}.
     * @param dto     DTO с данными для слотов
     * @param master  мастер
     * @param service услуга
     * @return сущность слотов
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "master", source = "master")
    @Mapping(target = "service", source = "service")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AvailabilitySlot toEntity(AvailabilitySlotDto dto, Master master, ServiceEntity service);
}