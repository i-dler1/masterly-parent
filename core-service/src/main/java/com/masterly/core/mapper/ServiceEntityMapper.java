package com.masterly.core.mapper;

import com.masterly.core.dto.ServiceEntityDto;
import com.masterly.core.model.ServiceEntity;
import com.masterly.core.model.Master;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;

@Component
public class ServiceEntityMapper {

    private static final Logger log = LoggerFactory.getLogger(ServiceEntityMapper.class);

    public ServiceEntityDto toDto(ServiceEntity service) {
        ServiceEntityDto dto = new ServiceEntityDto();
        dto.setId(service.getId());
        dto.setMasterId(service.getMaster().getId());
        dto.setName(service.getName());
        dto.setDescription(service.getDescription());
        dto.setDurationMinutes(service.getDurationMinutes());
        dto.setPrice(service.getPrice());
        dto.setCategory(service.getCategory());
        dto.setIsActive(service.getIsActive());
        dto.setCreatedAt(service.getCreatedAt());
        return dto;
    }

    public ServiceEntity toEntity(ServiceEntityDto dto, Master master) {
        log.debug("toEntity: dto={}", dto);
        log.debug("toEntity: master={}", master);
        ServiceEntity service = new ServiceEntity();
        service.setId(dto.getId());
        service.setMaster(master);
        service.setName(dto.getName());
        service.setDescription(dto.getDescription());
        service.setDurationMinutes(dto.getDurationMinutes());
        service.setPrice(dto.getPrice());
        service.setCategory(dto.getCategory());
        service.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return service;
    }
}