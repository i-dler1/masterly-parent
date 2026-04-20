package com.masterly.core.service;

import com.masterly.core.dto.AppointmentCreateDto;
import com.masterly.core.dto.AppointmentDto;
import com.masterly.core.mapper.AppointmentMapper;
import com.masterly.core.entity.*;
import com.masterly.core.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления записями.
 * Предоставляет бизнес-логику для создания, обновления, удаления записей,
 * а также управления статусами и списания/возврата материалов.
 */
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
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final MasterRepository masterRepository;

    /**
     * Получить все записи мастера.
     *
     * @param masterId ID мастера
     * @return список всех записей мастера
     */
    public List<Appointment> getAllAppointments(Long masterId) {
        log.debug("Fetching all appointments for master: {}", masterId);
        return appointmentRepository.findByMasterId(masterId);
    }

    /**
     * Получить запись по ID.
     *
     * @param id ID записи
     * @return запись или null если не найдена
     */
    public Appointment getAppointment(Long id) {
        log.debug("Fetching appointment by id: {}", id);
        return appointmentRepository.findById(id).orElse(null);
    }

    /**
     * Создать новую запись с проверкой доступности слота и наличия материалов.
     *
     * @param appointment запись для создания
     * @return созданная запись
     * @throws RuntimeException если слот занят или недостаточно материалов
     */
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        log.info("Creating appointment - master: {}, client: {}, service: {}, date: {}, time: {}",
                appointment.getMaster().getId(),
                appointment.getClient().getId(),
                appointment.getService().getId(),
                appointment.getAppointmentDate(),
                appointment.getStartTime());

        addClientToMasterIfNotExists(appointment.getMaster(), appointment.getClient());

        // Расчет времени окончания
        LocalTime endTime = appointment.getStartTime()
                .plusMinutes(appointment.getService().getDurationMinutes());
        appointment.setEndTime(endTime);
        log.debug("Calculated end time: {}", endTime);

        // Ищем слот
        List<AvailabilitySlot> slots = availabilitySlotRepository.findByMasterIdAndSlotDateAndStartTime(
                appointment.getMaster().getId(),
                appointment.getAppointmentDate(),
                appointment.getStartTime()
        );

        log.info("Found {} slots for master={}, date={}, time={}",
                slots.size(), appointment.getMaster().getId(),
                appointment.getAppointmentDate(), appointment.getStartTime());

        if (!slots.isEmpty()) {
            AvailabilitySlot slot = slots.get(0);
            log.info("Slot found: id={}, isBooked={}", slot.getId(), slot.getIsBooked());
            if (slot.getIsBooked()) {
                log.warn("Slot is already booked - master: {}, date: {}, time: {}",
                        appointment.getMaster().getId(),
                        appointment.getAppointmentDate(),
                        appointment.getStartTime());
                throw new RuntimeException("This time slot is already occupied");
            }
            slot.setIsBooked(true);
            availabilitySlotRepository.save(slot);
            log.info("Slot {} booked successfully", slot.getId());
        } else {
            log.warn("No slot found for master={}, date={}, time={}",
                    appointment.getMaster().getId(),
                    appointment.getAppointmentDate(),
                    appointment.getStartTime());
            boolean isOccupied = isTimeSlotOccupied(
                    appointment.getMaster().getId(),
                    appointment.getAppointmentDate(),
                    appointment.getStartTime(),
                    endTime
            );

            if (isOccupied) {
                throw new RuntimeException("This time slot is already occupied");
            }
        }

        // Проверка наличия материалов
        if (!hasEnoughMaterials(appointment.getService())) {
            throw new RuntimeException("Not enough materials for this service");
        }

        appointment.setStatus(AppointmentStatus.PENDING);
        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment created successfully with id: {}", saved.getId());

        writeOffMaterials(saved);

        return saved;
    }

    private void addClientToMasterIfNotExists(Master master, Client client) {
        // Проверяем, есть ли уже этот клиент у мастера
        boolean exists = clientRepository.existsByMasterAndId(master, client.getId());

        if (!exists) {
            log.info("Adding client {} to master {} clients list", client.getFullName(), master.getFullName());

            // Создаем связь клиента с мастером
            client.setMaster(master);
            client.setCreatedBy(master);
            clientRepository.save(client);

            log.info("Client {} added to master {} successfully", client.getFullName(), master.getFullName());
        } else {
            log.debug("Client {} already exists in master {} clients list", client.getFullName(), master.getFullName());
        }
    }

    /**
     * Создать новую запись на основе DTO.
     * Выполняет проверку доступности слота, наличия материалов и сохраняет запись.
     *
     * @param createDto DTO с данными для создания записи
     * @return DTO созданной записи
     * @throws RuntimeException если слот занят, недостаточно материалов или сущности не найдены
     */
    @Transactional
    public AppointmentDto createAppointment(AppointmentCreateDto createDto) {
        log.info("Creating appointment from DTO - masterId: {}, clientId: {}, serviceId: {}, date: {}, time: {}",
                createDto.getMasterId(), createDto.getClientId(), createDto.getServiceId(),
                createDto.getAppointmentDate(), createDto.getStartTime());

        // 1. Поиск связанных сущностей
        Master master = masterRepository.findById(createDto.getMasterId())
                .orElseThrow(() -> new RuntimeException("Master not found with id: " + createDto.getMasterId()));
        Client client = clientRepository.findById(createDto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + createDto.getClientId()));
        ServiceEntity service = serviceEntityRepository.findById(createDto.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found with id: " + createDto.getServiceId()));

        // 2. Маппинг DTO в сущность
        Appointment appointment = appointmentMapper.toEntity(createDto, master, client, service);

        // 3. Вызов существующего метода создания (он уже содержит всю бизнес-логику)
        Appointment saved = createAppointment(appointment);

        // 4. Преобразование результата в DTO
        return appointmentMapper.toDto(saved);
    }

    /**
     * Обновить статус записи. При завершении списывает материалы, при отмене — возвращает.
     *
     * @param id     ID записи
     * @param status новый статус
     * @return обновлённая запись или null если не найдена
     */
    @Transactional
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

        if (status == AppointmentStatus.CANCELLED && oldStatus != AppointmentStatus.COMPLETED) {
            log.info("Returning materials for cancelled appointment: {}", id);
            returnMaterials(appointment);
        }

        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment {} status updated successfully", id);
        return updated;
    }

    /**
     * Удалить запись. Если запись не завершена — возвращает материалы.
     *
     * @param id ID записи
     */
    public void deleteAppointment(Long id) {
        log.info("Deleting appointment: {}", id);

        Appointment appointment = getAppointment(id);
        if (appointment != null && appointment.getStatus() != AppointmentStatus.COMPLETED) {
            // Возвращаем материалы, если запись не была выполнена
            returnMaterials(appointment);
        }

        appointmentRepository.deleteById(id);
        log.debug("Appointment {} deleted", id);
    }

    /**
     * Проверить, занят ли временной слот.
     *
     * @param masterId  ID мастера
     * @param date      дата
     * @param startTime время начала
     * @param endTime   время окончания
     * @return true если слот занят, false если свободен
     */
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

    /**
     * Проверить, достаточно ли материалов для услуги.
     *
     * @param service услуга
     * @return true если материалов достаточно, false если нет
     */
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

    /**
     * Списать материалы для выполненной записи.
     *
     * @param appointment запись
     */
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

    /**
     * Получить записи мастера с пагинацией.
     *
     * @param masterId ID мастера
     * @param pageable параметры пагинации
     * @return страница с записями
     */
    public Page<AppointmentDto> getAppointmentsByMasterId(Long masterId, Pageable pageable) {
        log.debug("Fetching appointments for master: {}, page: {}, size: {}",
                masterId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Appointment> appointments = appointmentRepository.findByMasterId(masterId, pageable);
        log.debug("Found {} appointments", appointments.getTotalElements());

        return appointments.map(appointmentMapper::toDto);
    }

    /**
     * Обновить запись.
     *
     * @param id        ID записи
     * @param createDto DTO с новыми данными
     * @return обновлённая запись
     * @throws RuntimeException если запись не найдена или слот занят
     */
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

    /**
     * Получить все записи для администратора с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница со всеми записями
     */
    public Page<AppointmentDto> getAppointmentsForAdmin(Pageable pageable) {
        log.debug("Fetching all appointments for admin");
        Page<Appointment> appointments = appointmentRepository.findAll(pageable);
        return appointments.map(appointmentMapper::toDto);
    }

    /**
     * Получить записи клиента по ID.
     *
     * @param clientId ID клиента
     * @return список записей клиента с флагом justCreated для новых записей
     */
    public List<AppointmentDto> getAppointmentsByClientId(Long clientId) {
        List<Appointment> appointments = appointmentRepository.findByClientId(clientId);

        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);

        return appointments.stream()
                .map(appointment -> {
                    AppointmentDto dto = appointmentMapper.toDto(appointment);
                    // Устанавливаем флаг new, если запись создана менее 5 минут назад
                    dto.setJustCreated(appointment.getCreatedAt().isAfter(fiveMinutesAgo));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Вернуть материалы при отмене записи.
     *
     * @param appointment запись
     */
    public void returnMaterials(Appointment appointment) {
        log.info("Returning materials for appointment: {}", appointment.getId());

        List<ServiceMaterial> serviceMaterials = serviceMaterialRepository
                .findByServiceId(appointment.getService().getId());

        for (ServiceMaterial sm : serviceMaterials) {
            Material material = sm.getMaterial();
            BigDecimal newQuantity = material.getQuantity().add(sm.getQuantityUsed());
            material.setQuantity(newQuantity);
            materialRepository.save(material);
            log.info("Material returned: {} - {}, new quantity: {}",
                    material.getName(), sm.getQuantityUsed(), newQuantity);
        }
    }

    /**
     * Найти записи клиента по ID (альтернативный метод).
     *
     * @param clientId ID клиента
     * @return список записей клиента
     */
    public List<AppointmentDto> findByClientId(Long clientId) {
        log.debug("Finding appointments by client id: {}", clientId);

        List<Appointment> appointments = appointmentRepository.findByClientId(clientId);

        return appointments.stream()
                .map(appointmentMapper::toDto)
                .collect(Collectors.toList());
    }
}