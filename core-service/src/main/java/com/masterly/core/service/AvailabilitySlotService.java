package com.masterly.core.service;

import com.masterly.core.dto.AvailabilitySlotDto;
import com.masterly.core.mapper.AvailabilitySlotMapper;
import com.masterly.core.model.AvailabilitySlot;
import com.masterly.core.model.Master;
import com.masterly.core.model.ServiceEntity;
import com.masterly.core.repository.AvailabilitySlotRepository;
import com.masterly.core.repository.MasterRepository;
import com.masterly.core.repository.ServiceEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilitySlotService {

    private final AvailabilitySlotRepository slotRepository;
    private final MasterRepository masterRepository;
    private final ServiceEntityRepository serviceRepository;
    private final AvailabilitySlotMapper slotMapper;

    public List<AvailabilitySlotDto> getFreeSlots(Long masterId, Long serviceId, LocalDate date) {
        log.debug("Getting free slots for master: {}, service: {}, date: {}", masterId, serviceId, date);

        List<AvailabilitySlot> slots;
        if (serviceId != null) {
            slots = slotRepository.findByServiceIdAndSlotDateAndIsBookedFalse(serviceId, date);
        } else {
            slots = slotRepository.findByMasterIdAndSlotDateAndIsBookedFalse(masterId, date);
        }

        return slots.stream()
                .map(slotMapper::toDto)
                .collect(Collectors.toList());
    }

    public AvailabilitySlotDto createSlot(AvailabilitySlotDto slotDto) {
        log.info("Creating availability slot for master: {}, service: {}, date: {}, time: {}-{}",
                slotDto.getMasterId(), slotDto.getServiceId(), slotDto.getSlotDate(),
                slotDto.getStartTime(), slotDto.getEndTime());

        Master master = masterRepository.findById(slotDto.getMasterId())
                .orElseThrow(() -> new RuntimeException("Master not found"));

        ServiceEntity service = serviceRepository.findById(slotDto.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        AvailabilitySlot slot = slotMapper.toEntity(slotDto, master, service);
        AvailabilitySlot saved = slotRepository.save(slot);

        log.info("Slot created with id: {}", saved.getId());
        return slotMapper.toDto(saved);
    }

    public void bookSlot(Long slotId) {
        log.info("Booking slot: {}", slotId);

        AvailabilitySlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (slot.getIsBooked()) {
            throw new RuntimeException("Slot already booked");
        }

        slot.setIsBooked(true);
        slotRepository.save(slot);
        log.info("Slot {} booked successfully", slotId);
    }

    public void releaseSlot(Long slotId) {
        log.info("Releasing slot: {}", slotId);

        AvailabilitySlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        slot.setIsBooked(false);
        slotRepository.save(slot);
        log.info("Slot {} released", slotId);
    }

    public void deleteSlot(Long slotId) {
        log.info("Deleting slot: {}", slotId);
        AvailabilitySlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        slotRepository.delete(slot);
        log.info("Slot {} deleted", slotId);
    }
}