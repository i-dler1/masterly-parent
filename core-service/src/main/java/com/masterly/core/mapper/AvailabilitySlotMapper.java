package com.masterly.core.mapper;

import com.masterly.core.dto.AvailabilitySlotDto;
import com.masterly.core.model.AvailabilitySlot;
import com.masterly.core.model.Master;
import com.masterly.core.model.ServiceEntity;
import org.springframework.stereotype.Component;

@Component
public class AvailabilitySlotMapper {

    public AvailabilitySlotDto toDto(AvailabilitySlot slot) {
        if (slot == null) {
            return null;
        }

        AvailabilitySlotDto dto = new AvailabilitySlotDto();
        dto.setId(slot.getId());
        dto.setMasterId(slot.getMaster().getId());
        dto.setMasterName(slot.getMaster().getFullName());
        dto.setServiceId(slot.getService().getId());
        dto.setServiceName(slot.getService().getName());
        dto.setSlotDate(slot.getSlotDate());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setIsBooked(slot.getIsBooked());

        return dto;
    }

    public AvailabilitySlot toEntity(AvailabilitySlotDto dto, Master master, ServiceEntity service) {
        if (dto == null) {
            return null;
        }

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setMaster(master);
        slot.setService(service);
        slot.setSlotDate(dto.getSlotDate());
        slot.setStartTime(dto.getStartTime());
        slot.setEndTime(dto.getEndTime());
        slot.setIsBooked(dto.getIsBooked() != null ? dto.getIsBooked() : false);

        return slot;
    }
}