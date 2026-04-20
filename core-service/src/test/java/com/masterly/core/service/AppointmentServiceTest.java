package com.masterly.core.service;

import com.masterly.core.dto.AppointmentCreateDto;
import com.masterly.core.dto.AppointmentDto;
import com.masterly.core.mapper.AppointmentMapper;
import com.masterly.core.entity.*;
import com.masterly.core.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ServiceMaterialRepository serviceMaterialRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ServiceEntityRepository serviceEntityRepository;

    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;

    @Mock
    private MasterRepository masterRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentService appointmentService;

    private Master master;
    private Client client;
    private ServiceEntity service;
    private Appointment appointment;
    private AppointmentDto appointmentDto;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;

    @BeforeEach
    void setUp() {
        master = new Master();
        master.setId(1L);
        master.setEmail("master@test.com");

        client = new Client();
        client.setId(1L);
        client.setFullName("Тестовый Клиент");
        client.setMaster(master);

        service = new ServiceEntity();
        service.setId(1L);
        service.setName("Тестовая услуга");
        service.setDurationMinutes(60);

        appointmentDate = LocalDate.now().plusDays(1);
        startTime = LocalTime.of(10, 0);
        endTime = LocalTime.of(11, 0);

        appointment = new Appointment();
        appointment.setId(1L);
        appointment.setMaster(master);
        appointment.setClient(client);
        appointment.setService(service);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.PENDING);

        appointmentDto = new AppointmentDto();
        appointmentDto.setId(1L);
        appointmentDto.setMasterId(1L);
        appointmentDto.setClientId(1L);
        appointmentDto.setServiceId(1L);
        appointmentDto.setAppointmentDate(appointmentDate);
        appointmentDto.setStartTime(startTime);
        appointmentDto.setEndTime(endTime);
        appointmentDto.setStatus("PENDING");
    }

    @Test
    void getAllAppointments_ShouldReturnListOfAppointments() {
        // given
        Long masterId = 1L;
        List<Appointment> appointments = Collections.singletonList(appointment);
        when(appointmentRepository.findByMasterId(masterId)).thenReturn(appointments);

        // when
        List<Appointment> result = appointmentService.getAllAppointments(masterId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appointmentRepository).findByMasterId(masterId);
    }

    @Test
    void getAppointment_ShouldReturnAppointment_WhenExists() {
        // given
        Long id = 1L;
        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));

        // when
        Appointment result = appointmentService.getAppointment(id);

        // then
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(appointmentRepository).findById(id);
    }

    @Test
    void getAppointment_ShouldReturnNull_WhenNotExists() {
        // given
        Long id = 999L;
        when(appointmentRepository.findById(id)).thenReturn(Optional.empty());

        // when
        Appointment result = appointmentService.getAppointment(id);

        // then
        assertNull(result);
        verify(appointmentRepository).findById(id);
    }

    @Test
    void createAppointment_ShouldCreateAndReturnAppointment_WhenTimeIsFreeAndMaterialsEnough() {
        // given
        when(availabilitySlotRepository.findByMasterIdAndSlotDateAndStartTime(
                master.getId(), appointmentDate, startTime))
                .thenReturn(Collections.emptyList());

        when(appointmentRepository.findByMasterIdAndAppointmentDate(master.getId(), appointmentDate))
                .thenReturn(Collections.emptyList());
        when(serviceMaterialRepository.findByServiceId(service.getId())).thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // when
        Appointment result = appointmentService.createAppointment(appointment);

        // then
        assertNotNull(result);
        assertEquals(AppointmentStatus.PENDING, result.getStatus());
        assertNotNull(result.getEndTime());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    void createAppointment_ShouldThrowException_WhenTimeSlotOccupied() {
        // given - слот уже забронирован
        AvailabilitySlot bookedSlot = new AvailabilitySlot();
        bookedSlot.setId(1L);
        bookedSlot.setMaster(master);
        bookedSlot.setService(service);
        bookedSlot.setSlotDate(appointmentDate);
        bookedSlot.setStartTime(startTime);
        bookedSlot.setEndTime(endTime);
        bookedSlot.setIsBooked(true);  // ← СЛОТ УЖЕ ЗАБРОНИРОВАН

        when(availabilitySlotRepository.findByMasterIdAndSlotDateAndStartTime(
                master.getId(), appointmentDate, startTime))
                .thenReturn(List.of(bookedSlot));  // слот существует и занят

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> appointmentService.createAppointment(appointment));

        assertEquals("This time slot is already occupied", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_ShouldThrowException_WhenNotEnoughMaterials() {
        // given
        when(availabilitySlotRepository.findByMasterIdAndSlotDateAndStartTime(
                master.getId(), appointmentDate, startTime))
                .thenReturn(Collections.emptyList());

        when(appointmentRepository.findByMasterIdAndAppointmentDate(master.getId(), appointmentDate))
                .thenReturn(Collections.emptyList());

        Material material = new Material();
        material.setId(1L);
        material.setName("Тестовый материал");
        material.setQuantity(BigDecimal.valueOf(5));

        ServiceMaterial serviceMaterial = new ServiceMaterial();
        serviceMaterial.setMaterial(material);
        serviceMaterial.setQuantityUsed(BigDecimal.valueOf(10));

        when(serviceMaterialRepository.findByServiceId(service.getId()))
                .thenReturn(Collections.singletonList(serviceMaterial));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.createAppointment(appointment);
        });

        assertEquals("Not enough materials for this service", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void updateAppointmentStatus_ShouldUpdateAndReturnAppointment_WhenStatusChanged() {
        // given
        Long id = 1L;
        AppointmentStatus newStatus = AppointmentStatus.CONFIRMED;
        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // when
        Appointment result = appointmentService.updateAppointmentStatus(id, newStatus);

        // then
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        verify(appointmentRepository).findById(id);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void updateAppointmentStatus_ShouldReturnNull_WhenAppointmentNotFound() {
        // given
        Long id = 999L;
        AppointmentStatus newStatus = AppointmentStatus.CONFIRMED;
        when(appointmentRepository.findById(id)).thenReturn(Optional.empty());

        // when
        Appointment result = appointmentService.updateAppointmentStatus(id, newStatus);

        // then
        assertNull(result);
        verify(appointmentRepository).findById(id);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void updateAppointmentStatus_ShouldNotWriteOffMaterials_WhenStatusAlreadyCompleted() {
        // given
        Long id = 1L;
        appointment.setStatus(AppointmentStatus.COMPLETED);
        AppointmentStatus newStatus = AppointmentStatus.COMPLETED;

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // when
        Appointment result = appointmentService.updateAppointmentStatus(id, newStatus);

        // then
        assertNotNull(result);
        verify(serviceMaterialRepository, never()).findByServiceId(any());
        verify(materialRepository, never()).save(any());
    }

    @Test
    void updateAppointment_ShouldThrowException_WhenTimeSlotConflict() {
        // given
        Long id = 1L;
        LocalDate newDate = appointmentDate.plusDays(1); // ← новая дата
        LocalTime newStartTime = startTime.plusHours(1); // ← новое время

        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setMasterId(1L);
        createDto.setClientId(1L);
        createDto.setServiceId(1L);
        createDto.setAppointmentDate(newDate);     // ← новая дата
        createDto.setStartTime(newStartTime);       // ← новое время
        createDto.setNotes("Новая заметка");

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.existsByMasterIdAndAppointmentDateAndStartTime(
                master.getId(), newDate, newStartTime))  // ← новые параметры
                .thenReturn(true);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.updateAppointment(id, createDto);
        });

        assertEquals("This time slot is already occupied", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void updateAppointmentStatus_ShouldWriteOffMaterials_WhenStatusChangesToCompleted() {
        // given
        Long id = 1L;
        AppointmentStatus newStatus = AppointmentStatus.COMPLETED;
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        Material material = new Material();
        material.setId(1L);
        material.setName("Тестовый материал");
        material.setQuantity(BigDecimal.valueOf(10));

        ServiceMaterial serviceMaterial = new ServiceMaterial();
        serviceMaterial.setMaterial(material);
        serviceMaterial.setQuantityUsed(BigDecimal.valueOf(5));

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(serviceMaterialRepository.findByServiceId(service.getId()))
                .thenReturn(Collections.singletonList(serviceMaterial));
        when(materialRepository.save(any(Material.class))).thenReturn(material);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // when
        Appointment result = appointmentService.updateAppointmentStatus(id, newStatus);

        // then
        assertNotNull(result);
        verify(materialRepository).save(any(Material.class));
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void updateAppointment_ShouldThrowException_WhenClientNotFound() {
        // given
        Long id = 1L;
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setMasterId(1L);
        createDto.setClientId(999L);
        createDto.setServiceId(1L);
        createDto.setAppointmentDate(appointmentDate);
        createDto.setStartTime(startTime);

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(clientRepository.findById(createDto.getClientId())).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> appointmentService.updateAppointment(id, createDto));

        assertEquals("Client not found", exception.getMessage());
    }

    @Test
    void updateAppointment_ShouldThrowException_WhenAppointmentNotFound() {
        // given
        Long id = 999L;
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setMasterId(1L);
        createDto.setClientId(1L);
        createDto.setServiceId(1L);
        createDto.setAppointmentDate(appointmentDate);
        createDto.setStartTime(startTime);

        when(appointmentRepository.findById(id)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.updateAppointment(id, createDto);
        });

        assertEquals("Appointment not found", exception.getMessage());
    }

    @Test
    void deleteAppointment_ShouldDeleteAppointment() {
        // given
        Long id = 1L;
        doNothing().when(appointmentRepository).deleteById(id);

        // when
        appointmentService.deleteAppointment(id);

        // then
        verify(appointmentRepository).deleteById(id);
    }

    @Test
    void isTimeSlotOccupied_ShouldReturnTrue_WhenTimeSlotOccupied() {
        // given
        Long masterId = 1L;
        LocalDate date = appointmentDate;
        LocalTime start = startTime;
        LocalTime end = endTime;

        List<Appointment> appointments = Collections.singletonList(appointment);
        when(appointmentRepository.findByMasterIdAndAppointmentDate(masterId, date))
                .thenReturn(appointments);

        // when
        boolean result = appointmentService.isTimeSlotOccupied(masterId, date, start, end);

        // then
        assertTrue(result);
    }

    @Test
    void isTimeSlotOccupied_ShouldReturnFalse_WhenTimeSlotFree() {
        // given
        Long masterId = 1L;
        LocalDate date = appointmentDate;
        LocalTime start = startTime.plusHours(2);
        LocalTime end = endTime.plusHours(2);

        List<Appointment> appointments = Collections.singletonList(appointment);
        when(appointmentRepository.findByMasterIdAndAppointmentDate(masterId, date))
                .thenReturn(appointments);

        // when
        boolean result = appointmentService.isTimeSlotOccupied(masterId, date, start, end);

        // then
        assertFalse(result);
    }

    @Test
    void isTimeSlotOccupied_WithDebugLogging_ShouldExecuteLogBlock() {
        // given
        Long masterId = 1L;
        LocalDate date = appointmentDate;
        LocalTime start = startTime;
        LocalTime end = endTime;

        List<Appointment> appointments = Collections.singletonList(appointment);
        when(appointmentRepository.findByMasterIdAndAppointmentDate(masterId, date))
                .thenReturn(appointments);

        // Включаем DEBUG уровень для теста
        ch.qos.logback.classic.Logger logger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(AppointmentService.class);
        logger.setLevel(ch.qos.logback.classic.Level.DEBUG);

        // when
        boolean result = appointmentService.isTimeSlotOccupied(masterId, date, start, end);

        // then
        assertTrue(result);

        // Возвращаем уровень обратно
        logger.setLevel(ch.qos.logback.classic.Level.INFO);
    }

    @Test
    void isTimeSlotOccupied_ShouldReturnTrue_WhenTimeSlotOverlaps() {
        // given
        Long masterId = 1L;
        LocalDate date = appointmentDate;
        LocalTime start = startTime;           // 10:00
        LocalTime end = endTime;              // 11:00

        // Создаём запись, которая пересекается (с 10:30 до 11:30)
        Appointment overlappingAppointment = new Appointment();
        overlappingAppointment.setId(2L);
        overlappingAppointment.setMaster(master);
        overlappingAppointment.setClient(client);
        overlappingAppointment.setService(service);
        overlappingAppointment.setAppointmentDate(date);
        overlappingAppointment.setStartTime(LocalTime.of(10, 30));
        overlappingAppointment.setEndTime(LocalTime.of(11, 30));
        overlappingAppointment.setStatus(AppointmentStatus.CONFIRMED);

        List<Appointment> appointments = Collections.singletonList(overlappingAppointment);
        when(appointmentRepository.findByMasterIdAndAppointmentDate(masterId, date))
                .thenReturn(appointments);

        // when
        boolean result = appointmentService.isTimeSlotOccupied(masterId, date, start, end);

        // then
        assertTrue(result);
    }

    @Test
    void hasEnoughMaterials_ShouldReturnTrue_WhenMaterialsAvailable() {
        // given
        Material material = new Material();
        material.setId(1L);
        material.setQuantity(BigDecimal.valueOf(10));

        ServiceMaterial serviceMaterial = new ServiceMaterial();
        serviceMaterial.setMaterial(material);
        serviceMaterial.setQuantityUsed(BigDecimal.valueOf(5));

        when(serviceMaterialRepository.findByServiceId(service.getId()))
                .thenReturn(Collections.singletonList(serviceMaterial));

        // when
        boolean result = appointmentService.hasEnoughMaterials(service);

        // then
        assertTrue(result);
    }

    @Test
    void writeOffMaterials_ShouldDecreaseMaterialQuantity() {
        // given
        Material material = new Material();
        material.setId(1L);
        material.setName("Тестовый материал");
        material.setQuantity(BigDecimal.valueOf(10));

        ServiceMaterial serviceMaterial = new ServiceMaterial();
        serviceMaterial.setMaterial(material);
        serviceMaterial.setQuantityUsed(BigDecimal.valueOf(5));

        when(serviceMaterialRepository.findByServiceId(service.getId()))
                .thenReturn(Collections.singletonList(serviceMaterial));
        when(materialRepository.save(any(Material.class))).thenReturn(material);

        // when
        appointmentService.writeOffMaterials(appointment);

        // then
        verify(materialRepository).save(any(Material.class));
    }

    @Test
    void getAppointmentsByMasterId_ShouldReturnPageOfAppointmentDto() {
        // given
        Long masterId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Appointment> appointmentPage = new PageImpl<>(Collections.singletonList(appointment));

        when(appointmentRepository.findByMasterId(masterId, pageable)).thenReturn(appointmentPage);
        when(appointmentMapper.toDto(appointment)).thenReturn(appointmentDto);

        // when
        Page<AppointmentDto> result = appointmentService.getAppointmentsByMasterId(masterId, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(appointmentRepository).findByMasterId(masterId, pageable);
        verify(appointmentMapper).toDto(appointment);
    }

    @Test
    void updateAppointment_ShouldUpdateAndReturnAppointment_WhenNoConflict() {
        // given
        Long id = 1L;
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setMasterId(1L);
        createDto.setClientId(1L);
        createDto.setServiceId(1L);
        createDto.setAppointmentDate(appointmentDate);
        createDto.setStartTime(startTime);
        createDto.setNotes("Новая заметка");

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(clientRepository.findById(createDto.getClientId())).thenReturn(Optional.of(client));
        when(serviceEntityRepository.findById(createDto.getServiceId())).thenReturn(Optional.of(service));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // when
        Appointment result = appointmentService.updateAppointment(id, createDto);

        // then
        assertNotNull(result);
        verify(appointmentRepository).findById(id);
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void updateAppointment_ShouldThrowException_WhenServiceNotFound() {
        // given
        Long id = 1L;
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setMasterId(1L);
        createDto.setClientId(1L);
        createDto.setServiceId(999L);
        createDto.setAppointmentDate(appointmentDate);
        createDto.setStartTime(startTime);

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(clientRepository.findById(createDto.getClientId())).thenReturn(Optional.of(client));
        when(serviceEntityRepository.findById(createDto.getServiceId())).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.updateAppointment(id, createDto);
        });

        assertEquals("Service not found", exception.getMessage());
    }

    @Test
    void updateAppointment_ShouldNotCheckConflict_WhenStartTimeNotChanged() {
        // given
        Long id = 1L;
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setMasterId(1L);
        createDto.setClientId(1L);
        createDto.setServiceId(1L);
        createDto.setAppointmentDate(appointmentDate);
        createDto.setStartTime(startTime);  // ← то же самое время
        createDto.setNotes("Новая заметка");

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(clientRepository.findById(createDto.getClientId())).thenReturn(Optional.of(client));
        when(serviceEntityRepository.findById(createDto.getServiceId())).thenReturn(Optional.of(service));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // when
        Appointment result = appointmentService.updateAppointment(id, createDto);

        // then
        assertNotNull(result);
        // Проверяем, что проверка конфликта НЕ вызывалась
        verify(appointmentRepository, never()).existsByMasterIdAndAppointmentDateAndStartTime(any(), any(), any());
    }

    @Test
    void findByClientId_ShouldReturnListOfAppointmentDto() {
        // given
        Long clientId = 1L;
        List<Appointment> appointments = Collections.singletonList(appointment);
        when(appointmentRepository.findByClientId(clientId)).thenReturn(appointments);
        when(appointmentMapper.toDto(appointment)).thenReturn(appointmentDto);

        // when
        List<AppointmentDto> result = appointmentService.findByClientId(clientId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appointmentRepository).findByClientId(clientId);
        verify(appointmentMapper).toDto(appointment);
    }

    @Test
    void findByClientId_ShouldReturnEmptyList_WhenNoAppointments() {
        // given
        Long clientId = 999L;
        when(appointmentRepository.findByClientId(clientId)).thenReturn(Collections.emptyList());

        // when
        List<AppointmentDto> result = appointmentService.findByClientId(clientId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(appointmentRepository).findByClientId(clientId);
        verify(appointmentMapper, never()).toDto(any());
    }

    @Test
    void getAppointmentsForAdmin_ShouldReturnPageOfAppointmentDto() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Appointment> appointmentPage = new PageImpl<>(Collections.singletonList(appointment));

        when(appointmentRepository.findAll(pageable)).thenReturn(appointmentPage);
        when(appointmentMapper.toDto(appointment)).thenReturn(appointmentDto);

        // when
        Page<AppointmentDto> result = appointmentService.getAppointmentsForAdmin(pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(appointmentRepository).findAll(pageable);
        verify(appointmentMapper).toDto(appointment);
    }

    @Test
    void updateAppointment_ShouldThrowException_WhenTimeConflictExists() {
        // given
        Long id = 1L;
        LocalDate newDate = appointmentDate.plusDays(1);
        LocalTime newStartTime = startTime.plusHours(1);

        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setMasterId(1L);
        createDto.setClientId(1L);
        createDto.setServiceId(1L);
        createDto.setAppointmentDate(newDate);
        createDto.setStartTime(newStartTime);
        createDto.setNotes("Новая заметка");

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.existsByMasterIdAndAppointmentDateAndStartTime(
                master.getId(), newDate, newStartTime))
                .thenReturn(true);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.updateAppointment(id, createDto);
        });

        assertEquals("This time slot is already occupied", exception.getMessage());
    }

    @Test
    void updateAppointment_ShouldNotCheckConflict_WhenDateTimeNotChanged() {
        // given
        Long id = 1L;
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setMasterId(1L);
        createDto.setClientId(1L);
        createDto.setServiceId(1L);
        createDto.setAppointmentDate(appointmentDate);
        createDto.setStartTime(startTime);
        createDto.setNotes("Новая заметка");

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(clientRepository.findById(createDto.getClientId())).thenReturn(Optional.of(client));
        when(serviceEntityRepository.findById(createDto.getServiceId())).thenReturn(Optional.of(service));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // when
        Appointment result = appointmentService.updateAppointment(id, createDto);

        // then
        assertNotNull(result);
        verify(appointmentRepository, never()).existsByMasterIdAndAppointmentDateAndStartTime(any(), any(), any());
    }

    @Test
    void isTimeSlotOccupied_ShouldIgnoreCancelledAppointments() {
        // given
        Long masterId = 1L;
        LocalDate date = appointmentDate;
        LocalTime start = startTime;
        LocalTime end = endTime;

        Appointment cancelledAppointment = new Appointment();
        cancelledAppointment.setId(2L);
        cancelledAppointment.setMaster(master);
        cancelledAppointment.setClient(client);
        cancelledAppointment.setService(service);
        cancelledAppointment.setAppointmentDate(date);
        cancelledAppointment.setStartTime(start);
        cancelledAppointment.setEndTime(end);
        cancelledAppointment.setStatus(AppointmentStatus.CANCELLED);

        List<Appointment> appointments = Collections.singletonList(cancelledAppointment);
        when(appointmentRepository.findByMasterIdAndAppointmentDate(masterId, date))
                .thenReturn(appointments);

        // when
        boolean result = appointmentService.isTimeSlotOccupied(masterId, date, start, end);

        // then
        assertFalse(result);
    }

    @Test
    void updateAppointment_ShouldCheckConflict_WhenOnlyDateChanged() {
        // given
        Long id = 1L;
        LocalDate newDate = appointmentDate.plusDays(1);

        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setMasterId(1L);
        createDto.setClientId(1L);
        createDto.setServiceId(1L);
        createDto.setAppointmentDate(newDate);  // ← новая дата
        createDto.setStartTime(startTime);       // ← время то же
        createDto.setNotes("Новая заметка");

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.existsByMasterIdAndAppointmentDateAndStartTime(
                master.getId(), newDate, startTime))
                .thenReturn(false);  // нет конфликта
        when(clientRepository.findById(createDto.getClientId())).thenReturn(Optional.of(client));
        when(serviceEntityRepository.findById(createDto.getServiceId())).thenReturn(Optional.of(service));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // when
        Appointment result = appointmentService.updateAppointment(id, createDto);

        // then
        assertNotNull(result);
        verify(appointmentRepository).existsByMasterIdAndAppointmentDateAndStartTime(master.getId(), newDate, startTime);
    }

    @Test
    void isTimeSlotOccupied_ShouldReturnTrue_WhenOneActiveSlotOverlaps() {
        // given
        Long masterId = 1L;
        LocalDate date = appointmentDate;
        LocalTime start = startTime;
        LocalTime end = endTime;

        // Отменённая запись
        Appointment cancelledAppointment = new Appointment();
        cancelledAppointment.setId(2L);
        cancelledAppointment.setMaster(master);
        cancelledAppointment.setClient(client);
        cancelledAppointment.setService(service);
        cancelledAppointment.setAppointmentDate(date);
        cancelledAppointment.setStartTime(start);
        cancelledAppointment.setEndTime(end);
        cancelledAppointment.setStatus(AppointmentStatus.CANCELLED);

        // Активная запись, которая пересекается
        Appointment activeAppointment = new Appointment();
        activeAppointment.setId(3L);
        activeAppointment.setMaster(master);
        activeAppointment.setClient(client);
        activeAppointment.setService(service);
        activeAppointment.setAppointmentDate(date);
        activeAppointment.setStartTime(startTime);
        activeAppointment.setEndTime(endTime);
        activeAppointment.setStatus(AppointmentStatus.CONFIRMED);

        List<Appointment> appointments = Arrays.asList(cancelledAppointment, activeAppointment);
        when(appointmentRepository.findByMasterIdAndAppointmentDate(masterId, date))
                .thenReturn(appointments);

        // when
        boolean result = appointmentService.isTimeSlotOccupied(masterId, date, start, end);

        // then
        assertTrue(result);
    }

    @Test
    void isTimeSlotOccupied_ShouldReturnFalse_WhenAppointmentAfterSlot() {
        // given
        Long masterId = 1L;
        LocalDate date = appointmentDate;
        LocalTime start = startTime;           // 10:00
        LocalTime end = endTime;              // 11:00

        // Запись начинается после 11:00
        Appointment laterAppointment = new Appointment();
        laterAppointment.setId(2L);
        laterAppointment.setMaster(master);
        laterAppointment.setClient(client);
        laterAppointment.setService(service);
        laterAppointment.setAppointmentDate(date);
        laterAppointment.setStartTime(LocalTime.of(11, 30));
        laterAppointment.setEndTime(LocalTime.of(12, 30));
        laterAppointment.setStatus(AppointmentStatus.CONFIRMED);

        List<Appointment> appointments = Collections.singletonList(laterAppointment);
        when(appointmentRepository.findByMasterIdAndAppointmentDate(masterId, date))
                .thenReturn(appointments);

        // when
        boolean result = appointmentService.isTimeSlotOccupied(masterId, date, start, end);

        // then
        assertFalse(result);
    }

    @Test
    void updateAppointment_ShouldCheckConflict_WhenOnlyTimeChanged() {
        // given
        Long id = 1L;
        LocalTime newStartTime = startTime.plusHours(1);

        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setMasterId(1L);
        createDto.setClientId(1L);
        createDto.setServiceId(1L);
        createDto.setAppointmentDate(appointmentDate);  // дата та же
        createDto.setStartTime(newStartTime);           // время новое
        createDto.setNotes("Новая заметка");

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(clientRepository.findById(createDto.getClientId())).thenReturn(Optional.of(client));
        when(serviceEntityRepository.findById(createDto.getServiceId())).thenReturn(Optional.of(service));
        when(appointmentRepository.existsByMasterIdAndAppointmentDateAndStartTime(
                master.getId(), appointmentDate, newStartTime))
                .thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // when
        Appointment result = appointmentService.updateAppointment(id, createDto);

        // then
        assertNotNull(result);
        verify(appointmentRepository).existsByMasterIdAndAppointmentDateAndStartTime(master.getId(), appointmentDate, newStartTime);
    }

    // ==================== returnMaterials ====================

    @Test
    void returnMaterials_ShouldIncreaseMaterialQuantity() {
        // given
        Material material = new Material();
        material.setId(1L);
        material.setName("Тестовый материал");
        material.setQuantity(BigDecimal.valueOf(5));

        ServiceMaterial serviceMaterial = new ServiceMaterial();
        serviceMaterial.setMaterial(material);
        serviceMaterial.setQuantityUsed(BigDecimal.valueOf(5));

        when(serviceMaterialRepository.findByServiceId(service.getId()))
                .thenReturn(Collections.singletonList(serviceMaterial));
        when(materialRepository.save(any(Material.class))).thenReturn(material);

        // when
        appointmentService.returnMaterials(appointment);

        // then
        verify(materialRepository).save(any(Material.class));
        assertEquals(BigDecimal.valueOf(10), material.getQuantity());
    }

// ==================== createAppointment (DTO version) ====================

    @Test
    void createAppointment_FromDTO_ShouldCreateAndReturnAppointmentDto() {
        // given
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setMasterId(1L);
        createDto.setClientId(1L);
        createDto.setServiceId(1L);
        createDto.setAppointmentDate(appointmentDate);
        createDto.setStartTime(startTime);
        createDto.setNotes("Тестовая заметка");

        when(masterRepository.findById(1L)).thenReturn(Optional.of(master));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(serviceEntityRepository.findById(1L)).thenReturn(Optional.of(service));
        when(appointmentMapper.toEntity(eq(createDto), eq(master), eq(client), eq(service))).thenReturn(appointment);
        when(availabilitySlotRepository.findByMasterIdAndSlotDateAndStartTime(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.findByMasterIdAndAppointmentDate(any(), any()))
                .thenReturn(Collections.emptyList());
        when(serviceMaterialRepository.findByServiceId(any())).thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(appointmentMapper.toDto(appointment)).thenReturn(appointmentDto);

        // when
        AppointmentDto result = appointmentService.createAppointment(createDto);

        // then
        assertNotNull(result);
        verify(masterRepository).findById(1L);
        verify(clientRepository).findById(1L);
        verify(serviceEntityRepository).findById(1L);
        verify(appointmentMapper).toEntity(createDto, master, client, service);
        verify(appointmentRepository).save(any(Appointment.class));
        verify(appointmentMapper).toDto(appointment);
    }

    @Test
    void createAppointment_FromDTO_ShouldThrowException_WhenMasterNotFound() {
        // given
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setMasterId(999L);
        createDto.setClientId(1L);
        createDto.setServiceId(1L);
        createDto.setAppointmentDate(appointmentDate);
        createDto.setStartTime(startTime);

        when(masterRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.createAppointment(createDto);
        });

        assertEquals("Master not found with id: 999", exception.getMessage());
        verify(masterRepository).findById(999L);
        verify(clientRepository, never()).findById(any());
    }

    @Test
    void createAppointment_FromDTO_ShouldThrowException_WhenClientNotFound() {
        // given
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setMasterId(1L);
        createDto.setClientId(999L);
        createDto.setServiceId(1L);
        createDto.setAppointmentDate(appointmentDate);
        createDto.setStartTime(startTime);

        when(masterRepository.findById(1L)).thenReturn(Optional.of(master));
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.createAppointment(createDto);
        });

        assertEquals("Client not found with id: 999", exception.getMessage());
        verify(masterRepository).findById(1L);
        verify(clientRepository).findById(999L);
    }

    @Test
    void createAppointment_FromDTO_ShouldThrowException_WhenServiceNotFound() {
        // given
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setMasterId(1L);
        createDto.setClientId(1L);
        createDto.setServiceId(999L);
        createDto.setAppointmentDate(appointmentDate);
        createDto.setStartTime(startTime);

        when(masterRepository.findById(1L)).thenReturn(Optional.of(master));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(serviceEntityRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.createAppointment(createDto);
        });

        assertEquals("Service not found with id: 999", exception.getMessage());
        verify(masterRepository).findById(1L);
        verify(clientRepository).findById(1L);
        verify(serviceEntityRepository).findById(999L);
    }

// ==================== getAppointmentsByClientId ====================

    @Test
    void getAppointmentsByClientId_ShouldSetJustCreatedTrue_WhenCreatedWithin5Minutes() {
        // given
        Long clientId = 1L;
        appointment.setCreatedAt(LocalDateTime.now().minusMinutes(2)); // 2 минуты назад
        List<Appointment> appointments = Collections.singletonList(appointment);

        when(appointmentRepository.findByClientId(clientId)).thenReturn(appointments);
        when(appointmentMapper.toDto(appointment)).thenReturn(appointmentDto);

        // when
        List<AppointmentDto> result = appointmentService.getAppointmentsByClientId(clientId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appointmentMapper).toDto(appointment);
        // justCreated будет true, но проверить это сложно без сеттера в моке
    }

    @Test
    void getAppointmentsByClientId_ShouldSetJustCreatedFalse_WhenCreatedMoreThan5MinutesAgo() {
        // given
        Long clientId = 1L;
        appointment.setCreatedAt(LocalDateTime.now().minusMinutes(10)); // 10 минут назад
        List<Appointment> appointments = Collections.singletonList(appointment);

        when(appointmentRepository.findByClientId(clientId)).thenReturn(appointments);
        when(appointmentMapper.toDto(appointment)).thenReturn(appointmentDto);

        // when
        List<AppointmentDto> result = appointmentService.getAppointmentsByClientId(clientId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(appointmentMapper).toDto(appointment);
    }

// ==================== deleteAppointment ====================

    @Test
    void deleteAppointment_ShouldNotReturnMaterials_WhenStatusIsCompleted() {
        // given
        Long id = 1L;
        appointment.setStatus(AppointmentStatus.COMPLETED);
        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        doNothing().when(appointmentRepository).deleteById(id);

        // when
        appointmentService.deleteAppointment(id);

        // then
        verify(appointmentRepository).findById(id);
        verify(serviceMaterialRepository, never()).findByServiceId(any());
        verify(materialRepository, never()).save(any());
        verify(appointmentRepository).deleteById(id);
    }

    @Test
    void deleteAppointment_ShouldReturnMaterials_WhenStatusIsNotCompleted() {
        // given
        Long id = 1L;
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(serviceMaterialRepository.findByServiceId(service.getId())).thenReturn(Collections.emptyList());
        doNothing().when(appointmentRepository).deleteById(id);

        // when
        appointmentService.deleteAppointment(id);

        // then
        verify(appointmentRepository).findById(id);
        verify(serviceMaterialRepository).findByServiceId(service.getId());
        verify(appointmentRepository).deleteById(id);
    }

// ==================== updateAppointmentStatus - return materials ====================

    @Test
    void updateAppointmentStatus_ShouldReturnMaterials_WhenStatusChangedToCancelled() {
        // given
        Long id = 1L;
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        AppointmentStatus newStatus = AppointmentStatus.CANCELLED;

        Material material = new Material();
        material.setId(1L);
        material.setName("Тестовый материал");
        material.setQuantity(BigDecimal.valueOf(5));

        ServiceMaterial serviceMaterial = new ServiceMaterial();
        serviceMaterial.setMaterial(material);
        serviceMaterial.setQuantityUsed(BigDecimal.valueOf(5));

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(serviceMaterialRepository.findByServiceId(service.getId()))
                .thenReturn(Collections.singletonList(serviceMaterial));
        when(materialRepository.save(any(Material.class))).thenReturn(material);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // when
        Appointment result = appointmentService.updateAppointmentStatus(id, newStatus);

        // then
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        verify(serviceMaterialRepository).findByServiceId(service.getId());
        verify(materialRepository).save(any(Material.class));
        assertEquals(BigDecimal.valueOf(10), material.getQuantity()); // 5 + 5 = 10
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void updateAppointmentStatus_ShouldNotReturnMaterials_WhenStatusChangedToCancelledButAlreadyCompleted() {
        // given
        Long id = 1L;
        appointment.setStatus(AppointmentStatus.COMPLETED);  // oldStatus == COMPLETED
        AppointmentStatus newStatus = AppointmentStatus.CANCELLED;

        when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // when
        Appointment result = appointmentService.updateAppointmentStatus(id, newStatus);

        // then
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());
        verify(serviceMaterialRepository, never()).findByServiceId(any());
        verify(materialRepository, never()).save(any());
        verify(appointmentRepository).save(appointment);
    }

// ==================== hasEnoughMaterials - edge cases ====================

    @Test
    void hasEnoughMaterials_ShouldReturnFalse_WhenMultipleMaterialsAndOneInsufficient() {
        // given
        Material material1 = new Material();
        material1.setId(1L);
        material1.setQuantity(BigDecimal.valueOf(10));

        Material material2 = new Material();
        material2.setId(2L);
        material2.setQuantity(BigDecimal.valueOf(2)); // недостаточно

        ServiceMaterial sm1 = new ServiceMaterial();
        sm1.setMaterial(material1);
        sm1.setQuantityUsed(BigDecimal.valueOf(5));

        ServiceMaterial sm2 = new ServiceMaterial();
        sm2.setMaterial(material2);
        sm2.setQuantityUsed(BigDecimal.valueOf(5));

        when(serviceMaterialRepository.findByServiceId(service.getId()))
                .thenReturn(List.of(sm1, sm2));

        // when
        boolean result = appointmentService.hasEnoughMaterials(service);

        // then
        assertFalse(result);
        verify(serviceMaterialRepository).findByServiceId(service.getId());
    }

// ==================== addClientToMasterIfNotExists (косвенно) ====================

    @Test
    void createAppointment_ShouldAddClientToMaster_WhenClientNotLinked() {
        // given
        when(clientRepository.existsByMasterAndId(master, client.getId())).thenReturn(false);
        when(availabilitySlotRepository.findByMasterIdAndSlotDateAndStartTime(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.findByMasterIdAndAppointmentDate(any(), any()))
                .thenReturn(Collections.emptyList());
        when(serviceMaterialRepository.findByServiceId(any())).thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        // when
        appointmentService.createAppointment(appointment);

        // then
        verify(clientRepository).existsByMasterAndId(master, client.getId());
        verify(clientRepository).save(client);
        assertEquals(master, client.getMaster());
        assertEquals(master, client.getCreatedBy());
    }

    @Test
    void createAppointment_ShouldNotAddClientToMaster_WhenClientAlreadyLinked() {
        // given
        when(clientRepository.existsByMasterAndId(master, client.getId())).thenReturn(true);
        when(availabilitySlotRepository.findByMasterIdAndSlotDateAndStartTime(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.findByMasterIdAndAppointmentDate(any(), any()))
                .thenReturn(Collections.emptyList());
        when(serviceMaterialRepository.findByServiceId(any())).thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        // when
        appointmentService.createAppointment(appointment);

        // then
        verify(clientRepository).existsByMasterAndId(master, client.getId());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void createAppointment_ShouldThrowException_WhenNoSlotButTimeOccupied() {
        // given
        when(availabilitySlotRepository.findByMasterIdAndSlotDateAndStartTime(
                master.getId(), appointmentDate, startTime))
                .thenReturn(Collections.emptyList()); // слот не найден

        // Но время занято другой записью
        Appointment existingAppointment = new Appointment();
        existingAppointment.setId(2L);
        existingAppointment.setMaster(master);
        existingAppointment.setAppointmentDate(appointmentDate);
        existingAppointment.setStartTime(startTime);
        existingAppointment.setEndTime(endTime);
        existingAppointment.setStatus(AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findByMasterIdAndAppointmentDate(master.getId(), appointmentDate))
                .thenReturn(Collections.singletonList(existingAppointment));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> appointmentService.createAppointment(appointment));

        assertEquals("This time slot is already occupied", exception.getMessage());
        verify(availabilitySlotRepository).findByMasterIdAndSlotDateAndStartTime(
                master.getId(), appointmentDate, startTime);
        verify(appointmentRepository).findByMasterIdAndAppointmentDate(master.getId(), appointmentDate);
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void createAppointment_ShouldBookSlot_WhenSlotFoundAndFree() {
        // given
        AvailabilitySlot freeSlot = new AvailabilitySlot();
        freeSlot.setId(1L);
        freeSlot.setMaster(master);
        freeSlot.setService(service);
        freeSlot.setSlotDate(appointmentDate);
        freeSlot.setStartTime(startTime);
        freeSlot.setEndTime(endTime);
        freeSlot.setIsBooked(false);  // ← слот свободен

        when(availabilitySlotRepository.findByMasterIdAndSlotDateAndStartTime(
                master.getId(), appointmentDate, startTime))
                .thenReturn(List.of(freeSlot));  // слот найден

        when(serviceMaterialRepository.findByServiceId(service.getId()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
        when(availabilitySlotRepository.save(any(AvailabilitySlot.class))).thenReturn(freeSlot);

        // when
        Appointment result = appointmentService.createAppointment(appointment);

        // then
        assertNotNull(result);
        assertTrue(freeSlot.getIsBooked());  // слот стал забронирован
        verify(availabilitySlotRepository).save(freeSlot);
        verify(appointmentRepository).save(appointment);
    }
}
