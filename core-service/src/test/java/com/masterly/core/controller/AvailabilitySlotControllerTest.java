package com.masterly.core.controller;

import com.masterly.core.dto.AvailabilitySlotDto;
import com.masterly.core.service.AvailabilitySlotService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class AvailabilitySlotControllerTest {

    @Mock
    private AvailabilitySlotService slotService;

    @InjectMocks
    private AvailabilitySlotController slotController;

    private AvailabilitySlotDto slotDto;
    private Long masterId;
    private Long serviceId;
    private Long slotId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    @BeforeEach
    void setUp() {
        masterId = 1L;
        serviceId = 1L;
        slotId = 1L;
        date = LocalDate.now().plusDays(1);
        startTime = LocalTime.of(10, 0);
        endTime = LocalTime.of(11, 0);

        slotDto = new AvailabilitySlotDto();
        slotDto.setId(slotId);
        slotDto.setMasterId(masterId);
        slotDto.setServiceId(serviceId);
        slotDto.setSlotDate(date);
        slotDto.setStartTime(startTime);
        slotDto.setEndTime(endTime);
        slotDto.setIsBooked(false);
    }

    // ==================== getFreeSlots ====================

    @Test
    void getFreeSlots_ShouldReturnListOfSlots() {
        // given
        List<AvailabilitySlotDto> slots = Collections.singletonList(slotDto);
        when(slotService.getFreeSlots(masterId, serviceId, date)).thenReturn(slots);

        // when
        List<AvailabilitySlotDto> response = slotController.getFreeSlots(masterId, serviceId, date.toString());

        // then
        assertNotNull(response);
        assertEquals(1, response.size());
        verify(slotService).getFreeSlots(masterId, serviceId, date);
    }

    @Test
    void getFreeSlots_ShouldReturnEmptyList_WhenNoSlots() {
        // given
        when(slotService.getFreeSlots(masterId, serviceId, date)).thenReturn(Collections.emptyList());

        // when
        List<AvailabilitySlotDto> response = slotController.getFreeSlots(masterId, serviceId, date.toString());

        // then
        assertNotNull(response);
        assertTrue(response.isEmpty());
        verify(slotService).getFreeSlots(masterId, serviceId, date);
    }

    @Test
    void getFreeSlots_WithoutServiceId_ShouldReturnListOfSlots() {
        // given
        List<AvailabilitySlotDto> slots = Collections.singletonList(slotDto);
        when(slotService.getFreeSlots(masterId, null, date)).thenReturn(slots);

        // when
        List<AvailabilitySlotDto> response = slotController.getFreeSlots(masterId, null, date.toString());

        // then
        assertNotNull(response);
        assertEquals(1, response.size());
        verify(slotService).getFreeSlots(masterId, null, date);
    }

    // ==================== createSlot ====================

    @Test
    void createSlot_ShouldCreateAndReturnSlotDto() {
        // given
        when(slotService.createSlot(slotDto)).thenReturn(slotDto);

        // when
        AvailabilitySlotDto response = slotController.createSlot(slotDto);

        // then
        assertNotNull(response);
        assertEquals(slotId, response.getId());
        verify(slotService).createSlot(slotDto);
    }

    // ==================== bookSlot ====================

    @Test
    void bookSlot_ShouldReturnOk() {
        // given
        doNothing().when(slotService).bookSlot(slotId);

        // when
        ResponseEntity<Void> response = slotController.bookSlot(slotId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(slotService).bookSlot(slotId);
    }

    // ==================== releaseSlot ====================

    @Test
    void releaseSlot_ShouldReturnOk() {
        // given
        doNothing().when(slotService).releaseSlot(slotId);

        // when
        ResponseEntity<Void> response = slotController.releaseSlot(slotId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(slotService).releaseSlot(slotId);
    }

    // ==================== deleteSlot ====================

    @Test
    void deleteSlot_ShouldReturnNoContent() {
        // given
        doNothing().when(slotService).deleteSlot(slotId);

        // when
        ResponseEntity<Void> response = slotController.deleteSlot(slotId);

        // then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(slotService).deleteSlot(slotId);
    }

    @Test
    void getFreeSlotsByDateRange_ShouldReturnListOfSlots() {
        // given
        String startDate = "2024-03-01";
        String endDate = "2024-03-31";
        List<AvailabilitySlotDto> slots = Collections.singletonList(slotDto);
        when(slotService.getFreeSlotsByDateRange(masterId, serviceId, startDate, endDate)).thenReturn(slots);

        // when
        List<AvailabilitySlotDto> response = slotController.getFreeSlotsByDateRange(masterId, serviceId, startDate, endDate);

        // then
        assertNotNull(response);
        assertEquals(1, response.size());
        verify(slotService).getFreeSlotsByDateRange(masterId, serviceId, startDate, endDate);
    }

    @Test
    void getSlotsByDate_ShouldReturnListOfSlots() {
        // given
        String date = "2024-03-20";
        List<AvailabilitySlotDto> slots = Collections.singletonList(slotDto);
        when(slotService.getSlotsByDate(date)).thenReturn(slots);

        // when
        List<AvailabilitySlotDto> response = slotController.getSlotsByDate(date);

        // then
        assertNotNull(response);
        assertEquals(1, response.size());
        verify(slotService).getSlotsByDate(date);
    }

    @Test
    void getAllSlots_ShouldReturnListOfSlots() {
        // given
        List<AvailabilitySlotDto> slots = Collections.singletonList(slotDto);
        when(slotService.getAllSlots(masterId)).thenReturn(slots);

        // when
        List<AvailabilitySlotDto> response = slotController.getAllSlots(masterId);

        // then
        assertNotNull(response);
        assertEquals(1, response.size());
        verify(slotService).getAllSlots(masterId);
    }

    // ==================== releaseSlot (by appointment) ====================

    @Test
    void releaseSlot_ByAppointment_ShouldReturnOk() {
        // given
        String dateStr = date.toString();
        String startTimeStr = startTime.toString();
        doNothing().when(slotService).releaseSlotByAppointment(masterId, date, startTime);

        // when
        ResponseEntity<Void> response = slotController.releaseSlot(masterId, dateStr, startTimeStr);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(slotService).releaseSlotByAppointment(masterId, date, startTime);
    }
}