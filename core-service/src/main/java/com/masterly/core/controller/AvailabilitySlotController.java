package com.masterly.core.controller;

import com.masterly.core.dto.AvailabilitySlotDto;
import com.masterly.core.service.AvailabilitySlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Контроллер для управления слотами доступности мастера.
 * Предоставляет API для создания, бронирования, освобождения и удаления слотов.
 */
@Slf4j
@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilitySlotController {

    private final AvailabilitySlotService slotService;

    /**
     * Создать новый слот доступности.
     *
     * @param slotDto DTO с данными слота (masterId, serviceId, дата, время начала/окончания)
     * @return созданный слот
     */
    @PostMapping("/slots")
    public AvailabilitySlotDto createSlot(@Valid @RequestBody AvailabilitySlotDto slotDto) {
        log.info("=== CREATE SLOT ===");
        log.info("Received DTO: startTime={}, endTime={}", slotDto.getStartTime(), slotDto.getEndTime());
        return slotService.createSlot(slotDto);
    }

    /**
     * Забронировать слот по ID.
     *
     * @param slotId ID слота
     * @return 200 OK
     */
    @PostMapping("/slots/{slotId}/book")
    public ResponseEntity<Void> bookSlot(@PathVariable Long slotId) {
        log.info("POST /api/availability/slots/{}/book", slotId);
        slotService.bookSlot(slotId);
        return ResponseEntity.ok().build();
    }

    /**
     * Освободить ранее забронированный слот.
     *
     * @param slotId ID слота
     * @return 200 OK
     */
    @PostMapping("/slots/{slotId}/release")
    public ResponseEntity<Void> releaseSlot(@PathVariable Long slotId) {
        log.info("POST /api/availability/slots/{}/release", slotId);
        slotService.releaseSlot(slotId);
        return ResponseEntity.ok().build();
    }

    /**
     * Удалить слот по ID.
     *
     * @param slotId ID слота
     * @return 204 No Content
     */
    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long slotId) {
        log.info("DELETE /api/availability/slots/{}", slotId);
        slotService.deleteSlot(slotId);
        return ResponseEntity.noContent().build();
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
    @GetMapping("/slots-by-date-range")
    public List<AvailabilitySlotDto> getFreeSlotsByDateRange(
            @RequestParam Long masterId,
            @RequestParam Long serviceId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("Getting slots by date range: masterId={}, serviceId={}, startDate={}, endDate={}",
                masterId, serviceId, startDate, endDate);

        // Используем сервис
        return slotService.getFreeSlotsByDateRange(masterId, serviceId, startDate, endDate);
    }

    /**
     * Получить свободные слоты на конкретную дату.
     *
     * @param masterId  ID мастера
     * @param serviceId ID услуги (опционально)
     * @param date      дата (ISO формат: YYYY-MM-DD)
     * @return список свободных слотов
     */
    @GetMapping("/slots")
    public List<AvailabilitySlotDto> getFreeSlots(
            @RequestParam Long masterId,
            @RequestParam(required = false) Long serviceId,
            @RequestParam String date) {
        log.info("GET /api/availability/slots - master: {}, service: {}, date: {}", masterId, serviceId, date);
        LocalDate localDate = LocalDate.parse(date);
        return slotService.getFreeSlots(masterId, serviceId, localDate);
    }

    /**
     * Получить все слоты на указанную дату (без фильтрации по мастеру).
     *
     * @param date дата (ISO формат: YYYY-MM-DD)
     * @return список всех слотов за дату
     */
    @GetMapping("/slots/by-date")
    public List<AvailabilitySlotDto> getSlotsByDate(@RequestParam String date) {
        log.info("GET /api/availability/slots/by-date?date={}", date);
        return slotService.getSlotsByDate(date);
    }

    /**
     * Получить все слоты мастера.
     *
     * @param masterId ID мастера
     * @return список всех слотов мастера
     */
    @GetMapping("/slots/all")
    public List<AvailabilitySlotDto> getAllSlots(@RequestParam Long masterId) {
        log.info("GET /api/availability/slots/all?masterId={}", masterId);
        return slotService.getAllSlots(masterId);
    }

    /**
     * Освободить слот, связанный с отменённой записью.
     *
     * @param masterId  ID мастера
     * @param date      дата слота (ISO формат: YYYY-MM-DD)
     * @param startTime время начала (ISO формат: HH:MM)
     * @return 200 OK
     */
    @PostMapping("/slots/release")
    public ResponseEntity<Void> releaseSlot(@RequestParam Long masterId,
                                            @RequestParam String date,
                                            @RequestParam String startTime) {
        log.info("Releasing slot - master: {}, date: {}, time: {}", masterId, date, startTime);
        slotService.releaseSlotByAppointment(masterId, LocalDate.parse(date), LocalTime.parse(startTime));
        return ResponseEntity.ok().build();
    }
}