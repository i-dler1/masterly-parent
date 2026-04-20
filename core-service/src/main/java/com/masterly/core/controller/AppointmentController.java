package com.masterly.core.controller;

import com.masterly.core.dto.AppointmentCreateDto;
import com.masterly.core.dto.AppointmentDto;
import com.masterly.core.mapper.AppointmentMapper;
import com.masterly.core.entity.Appointment;
import com.masterly.core.entity.AppointmentStatus;
import com.masterly.core.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер для управления записями.
 * Предоставляет REST API для CRUD операций с записями.
 */
@Slf4j
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT_BY = "id";
    private static final String DEFAULT_SORT_DIR = "asc";
    private static final String SORT_ASC = "asc";

    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;

    /**
     * Получить запись по id.
     *
     * @param id идентификатор записи.
     * @return запись или 404 если не найдена.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDto> getAppointment(@PathVariable Long id) {
        log.debug("Fetching appointment by id: {}", id);
        Appointment appointment = appointmentService.getAppointment(id);
        if (appointment == null) {
            log.warn("Appointment not found with id: {}", id);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(appointmentMapper.toDto(appointment));
    }

    /**
     * Создать новую запись.
     *
     * @param createDto DTO с данными для создания записи
     * @return созданная запись
     */
    @PostMapping
    public ResponseEntity<AppointmentDto> createAppointment(@Valid @RequestBody AppointmentCreateDto createDto) {
        log.info("REST request to create appointment: {}", createDto);
        AppointmentDto created = appointmentService.createAppointment(createDto);
        return ResponseEntity.ok(created);
    }

    /**
     * Обновить статус записи.
     *
     * @param id     идентификатор записи
     * @param status новый статус (PENDING, CONFIRMED, COMPLETED, CANCELLED)
     * @return обновлённая запись или 404 если не найдена
     */
    @PostMapping("/{id}/status")
    public ResponseEntity<AppointmentDto> updateStatus(@PathVariable Long id,
                                                       @RequestParam AppointmentStatus status) {
        log.info("Updating appointment {} status to: {}", id, status);

        Appointment updated = appointmentService.updateAppointmentStatus(id, status);
        if (updated == null) {
            log.warn("Appointment not found for status update: {}", id);
            return ResponseEntity.notFound().build();
        }

        log.info("Appointment {} status updated to: {}", id, updated.getStatus());
        return ResponseEntity.ok(appointmentMapper.toDto(updated));
    }

    /**
     * Удалить запись по ID.
     *
     * @param id идентификатор записи
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        log.info("Deleting appointment: {}", id);
        appointmentService.deleteAppointment(id);
        log.debug("Appointment {} deleted", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получить записи мастера с пагинацией.
     *
     * @param page    номер страницы (0-based)
     * @param size    размер страницы
     * @param sortBy  поле для сортировки
     * @param sortDir направление сортировки (asc/desc)
     * @param masterId ID мастера
     * @return страница с записями
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<AppointmentDto>> getAppointmentsPaginated(
            @RequestParam(defaultValue = DEFAULT_PAGE + "") int page,
            @RequestParam(defaultValue = DEFAULT_SIZE + "") int size,
            @RequestParam(defaultValue = DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIR) String sortDir,
            @RequestParam Long masterId) {

        log.debug("Fetching appointments - master: {}, page: {}, size: {}, sortBy: {}, sortDir: {}",
                masterId, page, size, sortBy, sortDir);

        Sort sort = createSort(sortBy, sortDir);

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AppointmentDto> appointments = appointmentService.getAppointmentsByMasterId(masterId, pageable);

        log.debug("Found {} appointments total", appointments.getTotalElements());
        return ResponseEntity.ok(appointments);
    }

    /**
     * Получить записи мастера за указанный период.
     *
     * @param startDate начальная дата (ISO формат: YYYY-MM-DD)
     * @param endDate   конечная дата (ISO формат: YYYY-MM-DD)
     * @param masterId  ID мастера
     * @return список записей в указанном диапазоне дат
     */
    @GetMapping("/calendar")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam Long masterId) {

        log.debug("Fetching appointments by date range - master: {}, start: {}, end: {}",
                masterId, startDate, endDate);

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        List<Appointment> appointments = appointmentService.getAllAppointments(masterId);

        List<AppointmentDto> result = appointments.stream()
                .filter(a -> {
                    LocalDate appointmentDate = a.getAppointmentDate();
                    return !appointmentDate.isBefore(start) && !appointmentDate.isAfter(end);
                })
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());

        log.debug("Found {} appointments in date range", result.size());
        return ResponseEntity.ok(result);
    }

    /**
     * Обновить запись.
     *
     * @param id        идентификатор записи
     * @param createDto DTO с новыми данными
     * @return обновлённая запись или 404 если не найдена
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentDto> updateAppointment(@PathVariable Long id,
                                                            @Valid @RequestBody AppointmentCreateDto createDto) {
        log.info("Updating appointment: {}", id);

        Appointment updated = appointmentService.updateAppointment(id, createDto);
        if (updated == null) {
            log.warn("Appointment not found for update: {}", id);
            return ResponseEntity.notFound().build();
        }

        log.info("Appointment {} updated successfully", id);
        return ResponseEntity.ok(appointmentMapper.toDto(updated));
    }

    /**
     * Получить все записи клиента.
     *
     * @param clientId ID клиента
     * @return список записей клиента
     */
    @GetMapping("/by-client/{clientId}")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByClientId(@PathVariable Long clientId) {
        log.info("GET /api/appointments/by-client/{}", clientId);
        List<AppointmentDto> appointments = appointmentService.getAppointmentsByClientId(clientId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Проверить доступность временного слота для записи.
     *
     * @param masterId  ID мастера
     * @param date      дата (ISO формат: YYYY-MM-DD)
     * @param startTime время начала (ISO формат: HH:MM)
     * @param endTime   время окончания (ISO формат: HH:MM)
     * @return true если слот свободен, false если занят
     */
    @GetMapping("/check-availability")
    public ResponseEntity<Boolean> checkAvailability(
            @RequestParam Long masterId,
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam String endTime) {

        log.info("GET /api/appointments/check-availability - master: {}, date: {}, start: {}, end: {}",
                masterId, date, startTime, endTime);

        LocalDate appointmentDate = LocalDate.parse(date);
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);

        boolean isOccupied = appointmentService.isTimeSlotOccupied(masterId, appointmentDate, start, end);

        return ResponseEntity.ok(!isOccupied);
    }

    /**
     * Получить все записи для администратора с пагинацией.
     *
     * @param page    номер страницы (0-based)
     * @param size    размер страницы
     * @param sortBy  поле для сортировки
     * @param sortDir направление сортировки (asc/desc)
     * @return страница со всеми записями
     */
    @GetMapping("/admin/all")
    public ResponseEntity<Page<AppointmentDto>> getAllAppointmentsForAdmin(
            @RequestParam(defaultValue = DEFAULT_PAGE + "") int page,
            @RequestParam(defaultValue = DEFAULT_SIZE + "") int size,
            @RequestParam(defaultValue = DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIR) String sortDir) {

        log.info("GET /api/appointments/admin/all - admin requesting all appointments");
        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(appointmentService.getAppointmentsForAdmin(pageable));
    }

    private Sort createSort(String sortBy, String sortDir) {
        Sort sort;
        if (sortDir.equalsIgnoreCase(SORT_ASC)) {
            sort = Sort.by(sortBy).ascending();
        } else {
            sort = Sort.by(sortBy).descending();
        }
        return sort;
    }

    /**
     * Получить все записи мастера.
     *
     * @param masterId ID мастера
     * @return список всех записей мастера
     */
    @GetMapping("/by-master/{masterId}")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByMasterId(@PathVariable Long masterId) {
        log.info("GET /api/appointments/by-master/{}", masterId);
        List<AppointmentDto> appointments = appointmentService.getAllAppointments(masterId).stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(appointments);
    }
}