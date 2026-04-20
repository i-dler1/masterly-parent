package com.masterly.core.controller;

import com.masterly.core.dto.AppointmentCreateDto;
import com.masterly.core.dto.AppointmentDto;
import com.masterly.core.mapper.AppointmentMapper;
import com.masterly.core.entity.Appointment;
import com.masterly.core.entity.AppointmentStatus;
import com.masterly.core.entity.Client;
import com.masterly.core.entity.Master;
import com.masterly.core.entity.ServiceEntity;
import com.masterly.core.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentController appointmentController;

    private Master master;
    private Client client;
    private ServiceEntity service;
    private Appointment appointment;
    private AppointmentDto appointmentDto;
    private AppointmentCreateDto createDto;
    private Long masterId;
    private Long clientId;
    private Long serviceId;
    private Long appointmentId;

    @BeforeEach
    void setUp() {
        masterId = 1L;
        clientId = 1L;
        serviceId = 1L;
        appointmentId = 1L;

        master = new Master();
        master.setId(masterId);
        master.setEmail("master@test.com");
        master.setFullName("Мастер Тест");

        client = new Client();
        client.setId(clientId);
        client.setFullName("Тестовый Клиент");
        client.setMaster(master);

        service = new ServiceEntity();
        service.setId(serviceId);
        service.setName("Тестовая услуга");
        service.setDurationMinutes(60);

        LocalDate appointmentDate = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setMaster(master);
        appointment.setClient(client);
        appointment.setService(service);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.PENDING);

        appointmentDto = new AppointmentDto();
        appointmentDto.setId(appointmentId);
        appointmentDto.setMasterId(masterId);
        appointmentDto.setMasterName(master.getFullName());
        appointmentDto.setClientId(clientId);
        appointmentDto.setClientName(client.getFullName());
        appointmentDto.setServiceId(serviceId);
        appointmentDto.setServiceName(service.getName());
        appointmentDto.setAppointmentDate(appointmentDate);
        appointmentDto.setStartTime(startTime);
        appointmentDto.setEndTime(endTime);
        appointmentDto.setStatus("PENDING");

        createDto = new AppointmentCreateDto();
        createDto.setMasterId(masterId);
        createDto.setClientId(clientId);
        createDto.setServiceId(serviceId);
        createDto.setAppointmentDate(appointmentDate);
        createDto.setStartTime(startTime);
        createDto.setNotes("Тестовая заметка");
    }

    // ==================== getAppointment ====================

    @Test
    void getAppointment_ShouldReturnAppointmentDto_WhenExists() {
        when(appointmentService.getAppointment(appointmentId)).thenReturn(appointment);
        when(appointmentMapper.toDto(appointment)).thenReturn(appointmentDto);

        ResponseEntity<AppointmentDto> response = appointmentController.getAppointment(appointmentId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(appointmentId, response.getBody().getId());
        verify(appointmentService).getAppointment(appointmentId);
        verify(appointmentMapper).toDto(appointment);
    }

    @Test
    void getAppointment_ShouldReturnNotFound_WhenNotExists() {
        when(appointmentService.getAppointment(appointmentId)).thenReturn(null);

        ResponseEntity<AppointmentDto> response = appointmentController.getAppointment(appointmentId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(appointmentService).getAppointment(appointmentId);
        verify(appointmentMapper, never()).toDto(any());
    }

    // ==================== createAppointment ====================

    @Test
    void createAppointment_ShouldCreateAndReturnAppointmentDto() {
        when(appointmentService.createAppointment(any(AppointmentCreateDto.class)))
                .thenReturn(appointmentDto);

        ResponseEntity<AppointmentDto> response = appointmentController.createAppointment(createDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(appointmentDto, response.getBody());
        verify(appointmentService).createAppointment(createDto);
    }

    @Test
    void createAppointment_ShouldPropagateException_WhenServiceThrows() {
        when(appointmentService.createAppointment(any(AppointmentCreateDto.class)))
                .thenThrow(new RuntimeException("Time slot occupied"));

        assertThrows(RuntimeException.class, () -> {
            appointmentController.createAppointment(createDto);
        });
        verify(appointmentService).createAppointment(createDto);
    }

    // ==================== updateStatus ====================

    @Test
    void updateStatus_ShouldUpdateAndReturnAppointmentDto() {
        AppointmentStatus status = AppointmentStatus.CONFIRMED;
        when(appointmentService.updateAppointmentStatus(appointmentId, status)).thenReturn(appointment);
        when(appointmentMapper.toDto(appointment)).thenReturn(appointmentDto);

        ResponseEntity<AppointmentDto> response = appointmentController.updateStatus(appointmentId, status);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(appointmentService).updateAppointmentStatus(appointmentId, status);
        verify(appointmentMapper).toDto(appointment);
    }

    @Test
    void updateStatus_ShouldReturnNotFound_WhenAppointmentNotFound() {
        AppointmentStatus status = AppointmentStatus.CONFIRMED;
        when(appointmentService.updateAppointmentStatus(appointmentId, status)).thenReturn(null);

        ResponseEntity<AppointmentDto> response = appointmentController.updateStatus(appointmentId, status);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(appointmentService).updateAppointmentStatus(appointmentId, status);
        verify(appointmentMapper, never()).toDto(any());
    }

    // ==================== deleteAppointment ====================

    @Test
    void deleteAppointment_ShouldDeleteAndReturnNoContent() {
        doNothing().when(appointmentService).deleteAppointment(appointmentId);

        ResponseEntity<Void> response = appointmentController.deleteAppointment(appointmentId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(appointmentService).deleteAppointment(appointmentId);
    }

    // ==================== getAppointmentsPaginated ====================

    @Test
    void getAppointmentsPaginated_ShouldReturnPageOfAppointmentDto() {
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "asc";
        Page<AppointmentDto> appointmentPage = new PageImpl<>(Collections.singletonList(appointmentDto));

        when(appointmentService.getAppointmentsByMasterId(eq(masterId), any(Pageable.class)))
                .thenReturn(appointmentPage);

        ResponseEntity<Page<AppointmentDto>> response = appointmentController
                .getAppointmentsPaginated(page, size, sortBy, sortDir, masterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(appointmentService).getAppointmentsByMasterId(eq(masterId), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(page, capturedPageable.getPageNumber());
        assertEquals(size, capturedPageable.getPageSize());
        assertTrue(capturedPageable.getSort().isSorted());
        assertEquals("id", capturedPageable.getSort().getOrderFor("id").getProperty());
        assertEquals(Sort.Direction.ASC, capturedPageable.getSort().getOrderFor("id").getDirection());
    }

    @Test
    void getAppointmentsPaginated_WithDescSort_ShouldReturnPage() {
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "desc";
        Page<AppointmentDto> appointmentPage = new PageImpl<>(Collections.singletonList(appointmentDto));

        when(appointmentService.getAppointmentsByMasterId(eq(masterId), any(Pageable.class)))
                .thenReturn(appointmentPage);

        ResponseEntity<Page<AppointmentDto>> response = appointmentController
                .getAppointmentsPaginated(page, size, sortBy, sortDir, masterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(appointmentService).getAppointmentsByMasterId(eq(masterId), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(Sort.Direction.DESC, capturedPageable.getSort().getOrderFor("id").getDirection());
    }

    // ==================== getAppointmentsByDateRange ====================

    @Test
    void getAppointmentsByDateRange_ShouldReturnListOfAppointmentDto() {
        String startDate = "2026-04-01";
        String endDate = "2026-04-30";
        List<Appointment> appointments = Collections.singletonList(appointment);
        when(appointmentService.getAllAppointments(masterId)).thenReturn(appointments);
        when(appointmentMapper.toDto(appointment)).thenReturn(appointmentDto);

        ResponseEntity<List<AppointmentDto>> response = appointmentController
                .getAppointmentsByDateRange(startDate, endDate, masterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(appointmentService).getAllAppointments(masterId);
        verify(appointmentMapper).toDto(appointment);
    }

    @Test
    void getAppointmentsByDateRange_ShouldExcludeAppointmentBeforeStart() {
        LocalDate appointmentDate = LocalDate.of(2026, 4, 15);
        appointment.setAppointmentDate(appointmentDate);

        String startDate = "2026-04-16";
        String endDate = "2026-04-30";
        List<Appointment> appointments = Collections.singletonList(appointment);
        when(appointmentService.getAllAppointments(masterId)).thenReturn(appointments);

        ResponseEntity<List<AppointmentDto>> response = appointmentController
                .getAppointmentsByDateRange(startDate, endDate, masterId);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void getAppointmentsByDateRange_ShouldExcludeAppointmentAfterEnd() {
        LocalDate appointmentDate = LocalDate.of(2026, 4, 15);
        appointment.setAppointmentDate(appointmentDate);

        String startDate = "2026-04-01";
        String endDate = "2026-04-14";
        List<Appointment> appointments = Collections.singletonList(appointment);
        when(appointmentService.getAllAppointments(masterId)).thenReturn(appointments);

        ResponseEntity<List<AppointmentDto>> response = appointmentController
                .getAppointmentsByDateRange(startDate, endDate, masterId);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    // ==================== updateAppointment ====================

    @Test
    void updateAppointment_ShouldUpdateAndReturnAppointmentDto() {
        when(appointmentService.updateAppointment(appointmentId, createDto)).thenReturn(appointment);
        when(appointmentMapper.toDto(appointment)).thenReturn(appointmentDto);

        ResponseEntity<AppointmentDto> response = appointmentController.updateAppointment(appointmentId, createDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(appointmentService).updateAppointment(appointmentId, createDto);
        verify(appointmentMapper).toDto(appointment);
    }

    @Test
    void updateAppointment_ShouldReturnNotFound_WhenAppointmentNotFound() {
        when(appointmentService.updateAppointment(appointmentId, createDto)).thenReturn(null);

        ResponseEntity<AppointmentDto> response = appointmentController.updateAppointment(appointmentId, createDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(appointmentService).updateAppointment(appointmentId, createDto);
        verify(appointmentMapper, never()).toDto(any());
    }

    // ==================== getAppointmentsByClientId ====================

    @Test
    void getAppointmentsByClientId_ShouldReturnListOfAppointmentDto() {
        Long clientId = 1L;
        List<AppointmentDto> appointments = Collections.singletonList(appointmentDto);
        when(appointmentService.getAppointmentsByClientId(clientId)).thenReturn(appointments);

        ResponseEntity<List<AppointmentDto>> response = appointmentController.getAppointmentsByClientId(clientId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(appointmentService).getAppointmentsByClientId(clientId);
    }

    // ==================== getAllAppointmentsForAdmin ====================

    @Test
    void getAllAppointmentsForAdmin_ShouldReturnPageOfAppointmentDto() {
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "asc";
        Page<AppointmentDto> appointmentPage = new PageImpl<>(Collections.singletonList(appointmentDto));

        when(appointmentService.getAppointmentsForAdmin(any(Pageable.class))).thenReturn(appointmentPage);

        ResponseEntity<Page<AppointmentDto>> response = appointmentController
                .getAllAppointmentsForAdmin(page, size, sortBy, sortDir);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(appointmentService).getAppointmentsForAdmin(any(Pageable.class));
    }

    // ==================== getAppointmentsByMasterId ====================

    @Test
    void getAppointmentsByMasterId_ShouldReturnListOfAppointmentDto() {
        List<Appointment> appointments = Collections.singletonList(appointment);
        when(appointmentService.getAllAppointments(masterId)).thenReturn(appointments);
        when(appointmentMapper.toDto(appointment)).thenReturn(appointmentDto);

        ResponseEntity<List<AppointmentDto>> response = appointmentController.getAppointmentsByMasterId(masterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(appointmentService).getAllAppointments(masterId);
        verify(appointmentMapper).toDto(appointment);
    }

    // ==================== checkAvailability ====================

    @Test
    void checkAvailability_ShouldReturnTrue_WhenTimeSlotFree() {
        String date = "2026-04-10";
        String startTime = "10:00";
        String endTime = "11:00";

        when(appointmentService.isTimeSlotOccupied(eq(masterId), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(false);

        ResponseEntity<Boolean> response = appointmentController.checkAvailability(masterId, date, startTime, endTime);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody());
        verify(appointmentService).isTimeSlotOccupied(
                eq(masterId),
                eq(LocalDate.parse(date)),
                eq(LocalTime.parse(startTime)),
                eq(LocalTime.parse(endTime))
        );
    }

    @Test
    void checkAvailability_ShouldReturnFalse_WhenTimeSlotOccupied() {
        String date = "2026-04-10";
        String startTime = "10:00";
        String endTime = "11:00";

        when(appointmentService.isTimeSlotOccupied(eq(masterId), any(LocalDate.class), any(LocalTime.class), any(LocalTime.class)))
                .thenReturn(true);

        ResponseEntity<Boolean> response = appointmentController.checkAvailability(masterId, date, startTime, endTime);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody());
        verify(appointmentService).isTimeSlotOccupied(
                eq(masterId),
                eq(LocalDate.parse(date)),
                eq(LocalTime.parse(startTime)),
                eq(LocalTime.parse(endTime))
        );
    }
}