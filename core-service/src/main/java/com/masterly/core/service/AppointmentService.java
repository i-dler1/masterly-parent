package com.masterly.core.service;

import com.masterly.core.dto.AppointmentCreateDto;
import com.masterly.core.dto.AppointmentDto;
import com.masterly.core.mapper.AppointmentMapper;
import com.masterly.core.model.*;
import com.masterly.core.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ServiceMaterialRepository serviceMaterialRepository;
    private final MaterialRepository materialRepository;
    private final AppointmentMapper appointmentMapper;
    private final ClientRepository clientRepository;
    private final ServiceEntityRepository serviceEntityRepository;

    public List<Appointment> getAllAppointments(Long masterId) {
        log.debug("Fetching all appointments for master: {}", masterId);
        return appointmentRepository.findByMasterId(masterId);
    }

    public Appointment getAppointment(Long id) {
        log.debug("Fetching appointment by id: {}", id);
        return appointmentRepository.findById(id).orElse(null);
    }

    public Appointment createAppointment(Appointment appointment) {
        log.info("Creating appointment - master: {}, client: {}, service: {}, date: {}, time: {}",
                appointment.getMaster().getId(),
                appointment.getClient().getId(),
                appointment.getService().getId(),
                appointment.getAppointmentDate(),
                appointment.getStartTime());

        // Расчет времени окончания
        LocalTime endTime = appointment.getStartTime()
                .plusMinutes(appointment.getService().getDurationMinutes());
        appointment.setEndTime(endTime);
        log.debug("Calculated end time: {}", endTime);

        // Проверка конфликта времени
        boolean isOccupied = isTimeSlotOccupied(
                appointment.getMaster().getId(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                endTime
        );

        if (isOccupied) {
            log.warn("Time slot is occupied - master: {}, date: {}, time: {}",
                    appointment.getMaster().getId(),
                    appointment.getAppointmentDate(),
                    appointment.getStartTime());
            throw new RuntimeException("This time slot is already occupied");
        }
        log.debug("Time slot is free");

        // Проверка наличия материалов
        log.debug("Checking materials for service: {}", appointment.getService().getId());
        if (!hasEnoughMaterials(appointment.getService())) {
            log.warn("Not enough materials for service: {}", appointment.getService().getId());
            throw new RuntimeException("Not enough materials for this service");
        }
        log.debug("Materials check passed");

        appointment.setStatus(AppointmentStatus.PENDING);
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment created successfully with id: {}", saved.getId());

        return saved;
    }

    public Appointment updateAppointmentStatus(Long id, AppointmentStatus status) {
        log.info("Updating appointment {} status to: {}", id, status);

        Appointment appointment = getAppointment(id);
        if (appointment == null) {
            log.warn("Appointment not found for status update: {}", id);
            return null;
        }

        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(status);

        log.debug("Status changed from {} to {}", oldStatus, status);

        // Если статус меняется на COMPLETED, списываем материалы
        if (status == AppointmentStatus.COMPLETED && oldStatus != AppointmentStatus.COMPLETED) {
            log.info("Writing off materials for appointment: {}", id);
            writeOffMaterials(appointment);
        }

        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment {} status updated successfully", id);
        return updated;
    }

    public void deleteAppointment(Long id) {
        log.info("Deleting appointment: {}", id);
        appointmentRepository.deleteById(id);
        log.debug("Appointment {} deleted", id);
    }

    public boolean isTimeSlotOccupied(Long masterId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        log.debug("Checking time slot - master: {}, date: {}, start: {}, end: {}",
                masterId, date, startTime, endTime);

        List<Appointment> appointments = appointmentRepository.findByMasterIdAndAppointmentDate(masterId, date);
        log.debug("Found {} appointments for this date", appointments.size());

        if (log.isDebugEnabled()) {
            appointments.forEach(a ->
                    log.debug("Existing appointment - id: {}, start: {}, end: {}, status: {}",
                            a.getId(), a.getStartTime(), a.getEndTime(), a.getStatus())
            );
        }

        boolean occupied = appointments.stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .anyMatch(a -> !(endTime.isBefore(a.getStartTime()) || startTime.isAfter(a.getEndTime())));

        log.debug("Time slot is {}", occupied ? "occupied" : "free");
        return occupied;
    }

    public boolean hasEnoughMaterials(ServiceEntity service) {
        log.debug("Checking materials for service: {}", service.getId());

        List<ServiceMaterial> serviceMaterials = serviceMaterialRepository.findByServiceId(service.getId());
        log.debug("Found {} material links for service", serviceMaterials.size());

        if (serviceMaterials.isEmpty()) {
            log.debug("No materials required for this service");
            return true;
        }

        for (ServiceMaterial sm : serviceMaterials) {
            Material material = sm.getMaterial();
            BigDecimal needed = sm.getQuantityUsed();
            BigDecimal available = material.getQuantity();
            log.debug("Material: {}, needed: {}, available: {}", material.getName(), needed, available);

            if (available.compareTo(needed) < 0) {
                log.warn("Insufficient material: {}, needed: {}, available: {}",
                        material.getName(), needed, available);
                return false;
            }
        }

        log.debug("All materials available for service: {}", service.getId());
        return true;
    }

    public void writeOffMaterials(Appointment appointment) {
        log.info("Writing off materials for appointment: {}", appointment.getId());

        List<ServiceMaterial> serviceMaterials = serviceMaterialRepository
                .findByServiceId(appointment.getService().getId());

        for (ServiceMaterial sm : serviceMaterials) {
            Material material = sm.getMaterial();
            BigDecimal newQuantity = material.getQuantity().subtract(sm.getQuantityUsed());
            material.setQuantity(newQuantity);
            materialRepository.save(material);
            log.info("Material used: {} - {}, remaining: {}",
                    material.getName(), sm.getQuantityUsed(), newQuantity);
        }
    }

    public Page<AppointmentDto> getAppointmentsByMasterId(Long masterId, Pageable pageable) {
        log.debug("Fetching appointments for master: {}, page: {}, size: {}",
                masterId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Appointment> appointments = appointmentRepository.findByMasterId(masterId, pageable);
        log.debug("Found {} appointments", appointments.getTotalElements());

        return appointments.map(appointmentMapper::toDto);
    }

    public Appointment updateAppointment(Long id, AppointmentCreateDto createDto) {
        log.info("Updating appointment: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Appointment not found for update: {}", id);
                    return new RuntimeException("Appointment not found");
                });

        // Проверяем конфликт времени (если изменились дата или время)
        if (!appointment.getAppointmentDate().equals(createDto.getAppointmentDate()) ||
                !appointment.getStartTime().equals(createDto.getStartTime())) {
            log.debug("Date or time changed, checking for conflicts");

            boolean conflict = appointmentRepository.existsByMasterIdAndAppointmentDateAndStartTime(
                    appointment.getMaster().getId(),
                    createDto.getAppointmentDate(),
                    createDto.getStartTime()
            );

            if (conflict) {
                log.warn("Time slot conflict for appointment: {}", id);
                throw new RuntimeException("This time slot is already occupied");
            }
        }

        // Обновляем поля
        appointment.setAppointmentDate(createDto.getAppointmentDate());
        appointment.setStartTime(createDto.getStartTime());
        appointment.setNotes(createDto.getNotes());

        // Обновляем клиента и услугу
        Client client = clientRepository.findById(createDto.getClientId())
                .orElseThrow(() -> {
                    log.error("Client not found for update: {}", createDto.getClientId());
                    return new RuntimeException("Client not found");
                });
        ServiceEntity service = serviceEntityRepository.findById(createDto.getServiceId())
                .orElseThrow(() -> {
                    log.error("Service not found for update: {}", createDto.getServiceId());
                    return new RuntimeException("Service not found");
                });

        appointment.setClient(client);
        appointment.setService(service);

        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment {} updated successfully", id);

        return updated;
    }

    public List<AppointmentDto> findByClientId(Long clientId) {
        log.debug("Finding appointments by client id: {}", clientId);

        List<Appointment> appointments = appointmentRepository.findByClientId(clientId);
        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }
}