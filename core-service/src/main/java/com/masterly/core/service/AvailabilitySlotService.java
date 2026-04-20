package com.masterly.core.service;

import com.masterly.core.dto.AvailabilitySlotDto;
import com.masterly.core.mapper.AvailabilitySlotMapper;
import com.masterly.core.entity.AvailabilitySlot;
import com.masterly.core.entity.Master;
import com.masterly.core.entity.ServiceEntity;
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

/**
 * Сервис для управления слотами доступности мастера.
 * Предоставляет бизнес-логику для создания, бронирования, освобождения и удаления слотов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilitySlotService {

    private final AvailabilitySlotRepository slotRepository;
    private final MasterRepository masterRepository;
    private final ServiceEntityRepository serviceRepository;
    private final AvailabilitySlotMapper slotMapper;

    /**
     * Получить свободные слоты на конкретную дату.
     *
     * @param masterId  ID мастера
     * @param serviceId ID услуги (может быть null)
     * @param date      дата
     * @return список свободных слотов
     */
    public List<AvailabilitySlotDto> getFreeSlots(Long masterId, Long serviceId, LocalDate date) {
        if (serviceId != null) {
            return slotRepository.findByMasterIdAndServiceIdAndSlotDateAndIsBookedFalse(masterId, serviceId, date)
                    .stream().map(slotMapper::toDto).collect(Collectors.toList());
        } else {
            return slotRepository.findByMasterIdAndSlotDateAndIsBookedFalse(masterId, date)
                    .stream().map(slotMapper::toDto).collect(Collectors.toList());
        }
    }

    /**
     * Создать новый слот доступности.
     *
     * @param slotDto DTO с данными слота
     * @return созданный слот
     * @throws RuntimeException если слот уже существует или мастер/услуга не найдены
     */
    public AvailabilitySlotDto createSlot(AvailabilitySlotDto slotDto) {
        List<AvailabilitySlot> existing = slotRepository.findByMasterIdAndSlotDateAndStartTime(
                slotDto.getMasterId(),
                slotDto.getSlotDate(),
                slotDto.getStartTime()
        );

        if (!existing.isEmpty()) {
            log.warn("Slot already exists for master: {}, date: {}, time: {}",
                    slotDto.getMasterId(), slotDto.getSlotDate(), slotDto.getStartTime());
            throw new RuntimeException("Такой слот уже существует");
        }

        log.info("Creating availability slot for master: {}, service: {}, date: {}, time: {}-{}",
                slotDto.getMasterId(), slotDto.getServiceId(), slotDto.getSlotDate(),
                slotDto.getStartTime(), slotDto.getEndTime());

        Master master = masterRepository.findById(slotDto.getMasterId())
                .orElseThrow(() -> new RuntimeException("Master not found"));

        ServiceEntity service = serviceRepository.findById(slotDto.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        AvailabilitySlot slot = slotMapper.toEntity(slotDto, master, service);
        slot.setIsBooked(false);

        AvailabilitySlot saved = slotRepository.save(slot);

        log.info("Slot created with id: {}", saved.getId());
        return slotMapper.toDto(saved);
    }

    /**
     * Забронировать слот по ID.
     *
     * @param slotId ID слота
     * @throws RuntimeException если слот не найден или уже забронирован
     */
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

    /**
     * Освободить ранее забронированный слот.
     *
     * @param slotId ID слота
     * @throws RuntimeException если слот не найден
     */
    public void releaseSlot(Long slotId) {
        log.info("Releasing slot: {}", slotId);

        AvailabilitySlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        slot.setIsBooked(false);
        slotRepository.save(slot);
        log.info("Slot {} released", slotId);
    }

    /**
     * Удалить слот по ID.
     *
     * @param slotId ID слота
     * @throws RuntimeException если слот не найден
     */
    public void deleteSlot(Long slotId) {
        log.info("Deleting slot: {}", slotId);
        AvailabilitySlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        slotRepository.delete(slot);
        log.info("Slot {} deleted", slotId);
    }

    /**
     * Получить свободные слоты за указанный период.
     *
     * @param masterId  ID мастера
     * @param serviceId ID услуги
     * @param startDate начальная дата (ISO формат: YYYY-MM-DD)
     * @param endDate   конечная дата (ISO формат: YYYY-MM-DD)
     * @return список свободных слотов
     */
    public List<AvailabilitySlotDto> getFreeSlotsByDateRange(Long masterId, Long serviceId, String startDate, String endDate) {
        log.debug("Getting free slots by date range - master: {}, service: {}, start: {}, end: {}",
                masterId, serviceId, startDate, endDate);

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<AvailabilitySlot> slots = slotRepository
                .findByMasterIdAndServiceIdAndSlotDateBetweenAndIsBookedFalse(masterId, serviceId, start, end);

        return slots.stream()
                .map(slotMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить все слоты на указанную дату.
     *
     * @param date дата (ISO формат: YYYY-MM-DD)
     * @return список всех слотов за дату
     */
    public List<AvailabilitySlotDto> getSlotsByDate(String date) {
        log.debug("Getting slots by date: {}", date);
        LocalDate localDate = LocalDate.parse(date);
        List<AvailabilitySlot> slots = slotRepository.findBySlotDate(localDate);
        return slots.stream()
                .map(slotMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить все слоты мастера.
     *
     * @param masterId ID мастера
     * @return список всех слотов мастера
     */
    public List<AvailabilitySlotDto> getAllSlots(Long masterId) {
        log.debug("Getting all slots for master: {}", masterId);
        List<AvailabilitySlot> slots = slotRepository.findByMasterId(masterId);
        return slots.stream()
                .map(slotMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Освободить слот, связанный с отменённой записью.
     *
     * @param masterId  ID мастера
     * @param date      дата слота
     * @param startTime время начала
     */
    public void releaseSlotByAppointment(Long masterId, LocalDate date, LocalTime startTime) {
        log.info("Releasing slot for master: {}, date: {}, time: {}", masterId, date, startTime);

        List<AvailabilitySlot> slots = slotRepository.findByMasterIdAndSlotDateAndStartTime(masterId, date, startTime);

        if (slots.isEmpty()) {
            log.warn("Slot not found for release: master={}, date={}, time={}", masterId, date, startTime);
            return;
        }

        if (slots.size() > 1) {
            log.warn("Found {} slots for release, using first one", slots.size());
        }

        AvailabilitySlot slot = slots.get(0);
        slot.setIsBooked(false);
        slotRepository.save(slot);
        log.info("Slot {} released successfully", slot.getId());
    }
}