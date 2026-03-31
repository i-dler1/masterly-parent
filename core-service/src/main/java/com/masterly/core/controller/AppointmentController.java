package com.masterly.core.controller;

import com.masterly.core.dto.AppointmentCreateDto;
import com.masterly.core.dto.AppointmentDto;
import com.masterly.core.exception.TimeSlotOccupiedException;
import com.masterly.core.mapper.AppointmentMapper;
import com.masterly.core.model.Appointment;
import com.masterly.core.model.AppointmentStatus;
import com.masterly.core.model.Client;
import com.masterly.core.model.Master;
import com.masterly.core.model.ServiceEntity;
import com.masterly.core.repository.ClientRepository;
import com.masterly.core.repository.MasterRepository;
import com.masterly.core.repository.ServiceEntityRepository;
import com.masterly.core.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;
    private final MasterRepository masterRepository;
    private final ClientRepository clientRepository;
    private final ServiceEntityRepository serviceRepository;

    @GetMapping
    public ResponseEntity<List<AppointmentDto>> getAppointments(@RequestParam Long masterId) {
        log.debug("Fetching all appointments for master: {}", masterId);
        List<Appointment> appointments = appointmentService.getAllAppointments(masterId);
        List<AppointmentDto> dtos = appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
        log.debug("Found {} appointments", dtos.size());
        return ResponseEntity.ok(dtos);
    }

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

    @PostMapping
    public ResponseEntity<AppointmentDto> createAppointment(@Valid @RequestBody AppointmentCreateDto createDto) {
        log.info("Creating new appointment - client: {}, service: {}, date: {}, time: {}",
                createDto.getClientId(), createDto.getServiceId(),
                createDto.getAppointmentDate(), createDto.getStartTime());

        Master master = masterRepository.findById(createDto.getMasterId()).orElse(null);
        Client client = clientRepository.findById(createDto.getClientId()).orElse(null);
        ServiceEntity service = serviceRepository.findById(createDto.getServiceId()).orElse(null);

        if (master == null) {
            log.warn("Master not found with id: {}", createDto.getMasterId());
            return ResponseEntity.badRequest().build();
        }
        if (client == null) {
            log.warn("Client not found with id: {}", createDto.getClientId());
            return ResponseEntity.badRequest().build();
        }
        if (service == null) {
            log.warn("Service not found with id: {}", createDto.getServiceId());
            return ResponseEntity.badRequest().build();
        }

        log.debug("Found master: {}, client: {}, service: {}",
                master.getEmail(), client.getFullName(), service.getName());

        Appointment appointment = appointmentMapper.toEntity(createDto, master, client, service);
        Appointment saved = appointmentService.createAppointment(appointment);

        log.info("Appointment created successfully with id: {}", saved.getId());
        return ResponseEntity.ok(appointmentMapper.toDto(saved));
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        log.info("Deleting appointment: {}", id);
        appointmentService.deleteAppointment(id);
        log.debug("Appointment {} deleted", id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(TimeSlotOccupiedException.class)
    public ResponseEntity<String> handleTimeSlotOccupied(TimeSlotOccupiedException e) {
        log.warn("Time slot occupied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleConflict(RuntimeException e) {
        if (e.getMessage().equals("This time slot is already occupied")) {
            log.warn("Time slot conflict: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
        log.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<AppointmentDto>> getAppointmentsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam Long masterId) {

        log.debug("Fetching appointments - master: {}, page: {}, size: {}, sortBy: {}, sortDir: {}",
                masterId, page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AppointmentDto> appointments = appointmentService.getAppointmentsByMasterId(masterId, pageable);

        log.debug("Found {} appointments total", appointments.getTotalElements());
        return ResponseEntity.ok(appointments);
    }

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

    @GetMapping("/by-client/{clientId}")
    public ResponseEntity<List<AppointmentDto>> getAppointmentsByClientId(@PathVariable Long clientId) {
        log.info("GET /api/appointments/by-client/{}", clientId);
        List<AppointmentDto> appointments = appointmentService.findByClientId(clientId);
        return ResponseEntity.ok(appointments);
    }

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

        return ResponseEntity.ok(!isOccupied); // true - свободно, false - занято
    }
}