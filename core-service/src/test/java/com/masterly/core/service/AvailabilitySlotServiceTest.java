package com.masterly.core.service;

import com.masterly.core.dto.AvailabilitySlotDto;
import com.masterly.core.mapper.AvailabilitySlotMapper;
import com.masterly.core.entity.AvailabilitySlot;
import com.masterly.core.entity.Master;
import com.masterly.core.entity.ServiceEntity;
import com.masterly.core.repository.AvailabilitySlotRepository;
import com.masterly.core.repository.MasterRepository;
import com.masterly.core.repository.ServiceEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilitySlotServiceTest {

    @Mock
    private AvailabilitySlotRepository slotRepository;

    @Mock
    private MasterRepository masterRepository;

    @Mock
    private ServiceEntityRepository serviceRepository;

    @Mock
    private AvailabilitySlotMapper slotMapper;

    @InjectMocks
    private AvailabilitySlotService slotService;

    private Master master;
    private ServiceEntity service;
    private AvailabilitySlot slot;
    private AvailabilitySlotDto slotDto;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;

    @BeforeEach
    void setUp() {
        master = new Master();
        master.setId(1L);
        master.setEmail("master@test.com");

        service = new ServiceEntity();
        service.setId(1L);
        service.setName("Тестовая услуга");
        service.setDurationMinutes(60);

        slotDate = LocalDate.now().plusDays(1);
        startTime = LocalTime.of(10, 0);
        endTime = LocalTime.of(11, 0);

        slot = new AvailabilitySlot();
        slot.setId(1L);
        slot.setMaster(master);
        slot.setService(service);
        slot.setSlotDate(slotDate);
        slot.setStartTime(startTime);
        slot.setEndTime(endTime);
        slot.setIsBooked(false);

        slotDto = new AvailabilitySlotDto();
        slotDto.setId(1L);
        slotDto.setMasterId(1L);
        slotDto.setServiceId(1L);
        slotDto.setSlotDate(slotDate);
        slotDto.setStartTime(startTime);
        slotDto.setEndTime(endTime);
        slotDto.setIsBooked(false);
    }

    // ==================== getFreeSlots ====================

    @Test
    void getFreeSlots_ShouldReturnListOfSlots_WhenServiceIdProvided() {
        // given
        Long masterId = 1L;
        Long serviceId = 1L;
        List<AvailabilitySlot> slots = Collections.singletonList(slot);

        // ИСПРАВЛЕНО: правильный метод с masterId
        when(slotRepository.findByMasterIdAndServiceIdAndSlotDateAndIsBookedFalse(
                masterId, serviceId, slotDate))
                .thenReturn(slots);
        when(slotMapper.toDto(slot)).thenReturn(slotDto);

        // when
        List<AvailabilitySlotDto> result = slotService.getFreeSlots(masterId, serviceId, slotDate);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(slotRepository).findByMasterIdAndServiceIdAndSlotDateAndIsBookedFalse(
                masterId, serviceId, slotDate);
        verify(slotMapper).toDto(slot);
    }

    @Test
    void getFreeSlots_ShouldReturnListOfSlots_WhenServiceIdNotProvided() {
        // given
        Long masterId = 1L;
        List<AvailabilitySlot> slots = Collections.singletonList(slot);

        when(slotRepository.findByMasterIdAndSlotDateAndIsBookedFalse(masterId, slotDate))
                .thenReturn(slots);
        when(slotMapper.toDto(slot)).thenReturn(slotDto);

        // when
        List<AvailabilitySlotDto> result = slotService.getFreeSlots(masterId, null, slotDate);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(slotRepository).findByMasterIdAndSlotDateAndIsBookedFalse(masterId, slotDate);
        verify(slotMapper).toDto(slot);
    }

    @Test
    void getFreeSlots_ShouldReturnEmptyList_WhenNoSlots() {
        // given
        Long masterId = 1L;
        Long serviceId = null;
        when(slotRepository.findByMasterIdAndSlotDateAndIsBookedFalse(masterId, slotDate))
                .thenReturn(Collections.emptyList());

        // when
        List<AvailabilitySlotDto> result = slotService.getFreeSlots(masterId, serviceId, slotDate);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(slotRepository).findByMasterIdAndSlotDateAndIsBookedFalse(masterId, slotDate);
        verify(slotMapper, never()).toDto(any());
    }

    // ==================== createSlot ====================

    @Test
    void createSlot_ShouldCreateAndReturnSlotDto() {
        // given
        when(masterRepository.findById(slotDto.getMasterId())).thenReturn(Optional.of(master));
        when(serviceRepository.findById(slotDto.getServiceId())).thenReturn(Optional.of(service));
        when(slotMapper.toEntity(slotDto, master, service)).thenReturn(slot);
        when(slotRepository.save(slot)).thenReturn(slot);
        when(slotMapper.toDto(slot)).thenReturn(slotDto);

        // when
        AvailabilitySlotDto result = slotService.createSlot(slotDto);

        // then
        assertNotNull(result);
        verify(masterRepository).findById(slotDto.getMasterId());
        verify(serviceRepository).findById(slotDto.getServiceId());
        verify(slotMapper).toEntity(slotDto, master, service);
        verify(slotRepository).save(slot);
        verify(slotMapper).toDto(slot);
    }

    @Test
    void createSlot_ShouldThrowException_WhenMasterNotFound() {
        // given
        when(masterRepository.findById(slotDto.getMasterId())).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            slotService.createSlot(slotDto);
        });

        assertEquals("Master not found", exception.getMessage());
        verify(serviceRepository, never()).findById(any());
        verify(slotRepository, never()).save(any());
    }

    @Test
    void createSlot_ShouldThrowException_WhenServiceNotFound() {
        // given
        when(masterRepository.findById(slotDto.getMasterId())).thenReturn(Optional.of(master));
        when(serviceRepository.findById(slotDto.getServiceId())).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            slotService.createSlot(slotDto);
        });

        assertEquals("Service not found", exception.getMessage());
        verify(slotRepository, never()).save(any());
    }

    // ==================== bookSlot ====================

    @Test
    void bookSlot_ShouldBookSlot_WhenSlotFree() {
        // given
        Long slotId = 1L;
        slot.setIsBooked(false);
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        when(slotRepository.save(slot)).thenReturn(slot);

        // when
        slotService.bookSlot(slotId);

        // then
        assertTrue(slot.getIsBooked());
        verify(slotRepository).findById(slotId);
        verify(slotRepository).save(slot);
    }

    @Test
    void bookSlot_ShouldThrowException_WhenSlotAlreadyBooked() {
        // given
        Long slotId = 1L;
        slot.setIsBooked(true);
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            slotService.bookSlot(slotId);
        });

        assertEquals("Slot already booked", exception.getMessage());
        verify(slotRepository).findById(slotId);
        verify(slotRepository, never()).save(any());
    }

    @Test
    void bookSlot_ShouldThrowException_WhenSlotNotFound() {
        // given
        Long slotId = 999L;
        when(slotRepository.findById(slotId)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            slotService.bookSlot(slotId);
        });

        assertEquals("Slot not found", exception.getMessage());
        verify(slotRepository).findById(slotId);
        verify(slotRepository, never()).save(any());
    }

    // ==================== releaseSlot ====================

    @Test
    void releaseSlot_ShouldReleaseSlot_WhenSlotBooked() {
        // given
        Long slotId = 1L;
        slot.setIsBooked(true);
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        when(slotRepository.save(slot)).thenReturn(slot);

        // when
        slotService.releaseSlot(slotId);

        // then
        assertFalse(slot.getIsBooked());
        verify(slotRepository).findById(slotId);
        verify(slotRepository).save(slot);
    }

    @Test
    void releaseSlot_ShouldThrowException_WhenSlotNotFound() {
        // given
        Long slotId = 999L;
        when(slotRepository.findById(slotId)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            slotService.releaseSlot(slotId);
        });

        assertEquals("Slot not found", exception.getMessage());
        verify(slotRepository).findById(slotId);
        verify(slotRepository, never()).save(any());
    }

    // ==================== deleteSlot ====================

    @Test
    void deleteSlot_ShouldDeleteSlot() {
        // given
        Long slotId = 1L;
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        doNothing().when(slotRepository).delete(slot);

        // when
        slotService.deleteSlot(slotId);

        // then
        verify(slotRepository).findById(slotId);
        verify(slotRepository).delete(slot);
    }

    @Test
    void deleteSlot_ShouldThrowException_WhenSlotNotFound() {
        // given
        Long slotId = 999L;
        when(slotRepository.findById(slotId)).thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            slotService.deleteSlot(slotId);
        });

        assertEquals("Slot not found", exception.getMessage());
        verify(slotRepository).findById(slotId);
        verify(slotRepository, never()).delete(any());
    }

    // ==================== createSlot - existing slot check ====================

    @Test
    void createSlot_ShouldThrowException_WhenSlotAlreadyExists() {
        // given
        when(slotRepository.findByMasterIdAndSlotDateAndStartTime(
                slotDto.getMasterId(), slotDto.getSlotDate(), slotDto.getStartTime()))
                .thenReturn(Collections.singletonList(slot));

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            slotService.createSlot(slotDto);
        });

        assertEquals("Такой слот уже существует", exception.getMessage());
        verify(slotRepository).findByMasterIdAndSlotDateAndStartTime(
                slotDto.getMasterId(), slotDto.getSlotDate(), slotDto.getStartTime());
        verify(masterRepository, never()).findById(any());
        verify(serviceRepository, never()).findById(any());
        verify(slotRepository, never()).save(any());
    }

// ==================== getFreeSlotsByDateRange ====================

    @Test
    void getFreeSlotsByDateRange_ShouldReturnListOfSlots() {
        // given
        Long masterId = 1L;
        Long serviceId = 1L;
        String startDate = "2026-04-01";
        String endDate = "2026-04-30";
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        List<AvailabilitySlot> slots = Collections.singletonList(slot);

        when(slotRepository.findByMasterIdAndServiceIdAndSlotDateBetweenAndIsBookedFalse(
                masterId, serviceId, start, end))
                .thenReturn(slots);
        when(slotMapper.toDto(slot)).thenReturn(slotDto);

        // when
        List<AvailabilitySlotDto> result = slotService.getFreeSlotsByDateRange(masterId, serviceId, startDate, endDate);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(slotRepository).findByMasterIdAndServiceIdAndSlotDateBetweenAndIsBookedFalse(
                masterId, serviceId, start, end);
        verify(slotMapper).toDto(slot);
    }

    @Test
    void getFreeSlotsByDateRange_ShouldReturnEmptyList_WhenNoSlots() {
        // given
        Long masterId = 1L;
        Long serviceId = 1L;
        String startDate = "2026-04-01";
        String endDate = "2026-04-30";
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        when(slotRepository.findByMasterIdAndServiceIdAndSlotDateBetweenAndIsBookedFalse(
                masterId, serviceId, start, end))
                .thenReturn(Collections.emptyList());

        // when
        List<AvailabilitySlotDto> result = slotService.getFreeSlotsByDateRange(masterId, serviceId, startDate, endDate);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(slotRepository).findByMasterIdAndServiceIdAndSlotDateBetweenAndIsBookedFalse(
                masterId, serviceId, start, end);
        verify(slotMapper, never()).toDto(any());
    }

// ==================== getSlotsByDate ====================

    @Test
    void getSlotsByDate_ShouldReturnListOfSlots() {
        // given
        String date = "2026-04-15";
        LocalDate localDate = LocalDate.parse(date);
        List<AvailabilitySlot> slots = Collections.singletonList(slot);

        when(slotRepository.findBySlotDate(localDate)).thenReturn(slots);
        when(slotMapper.toDto(slot)).thenReturn(slotDto);

        // when
        List<AvailabilitySlotDto> result = slotService.getSlotsByDate(date);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(slotRepository).findBySlotDate(localDate);
        verify(slotMapper).toDto(slot);
    }

    @Test
    void getSlotsByDate_ShouldReturnEmptyList_WhenNoSlots() {
        // given
        String date = "2026-04-15";
        LocalDate localDate = LocalDate.parse(date);

        when(slotRepository.findBySlotDate(localDate)).thenReturn(Collections.emptyList());

        // when
        List<AvailabilitySlotDto> result = slotService.getSlotsByDate(date);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(slotRepository).findBySlotDate(localDate);
        verify(slotMapper, never()).toDto(any());
    }

// ==================== getAllSlots ====================

    @Test
    void getAllSlots_ShouldReturnListOfSlots() {
        // given
        Long masterId = 1L;
        List<AvailabilitySlot> slots = Collections.singletonList(slot);

        when(slotRepository.findByMasterId(masterId)).thenReturn(slots);
        when(slotMapper.toDto(slot)).thenReturn(slotDto);

        // when
        List<AvailabilitySlotDto> result = slotService.getAllSlots(masterId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(slotRepository).findByMasterId(masterId);
        verify(slotMapper).toDto(slot);
    }

    @Test
    void getAllSlots_ShouldReturnEmptyList_WhenNoSlots() {
        // given
        Long masterId = 1L;

        when(slotRepository.findByMasterId(masterId)).thenReturn(Collections.emptyList());

        // when
        List<AvailabilitySlotDto> result = slotService.getAllSlots(masterId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(slotRepository).findByMasterId(masterId);
        verify(slotMapper, never()).toDto(any());
    }

// ==================== releaseSlotByAppointment ====================

    @Test
    void releaseSlotByAppointment_ShouldReleaseSlot_WhenSlotExists() {
        // given
        Long masterId = 1L;
        LocalDate date = slotDate;
        LocalTime time = startTime;
        List<AvailabilitySlot> slots = Collections.singletonList(slot);
        slot.setIsBooked(true);

        when(slotRepository.findByMasterIdAndSlotDateAndStartTime(masterId, date, time))
                .thenReturn(slots);
        when(slotRepository.save(slot)).thenReturn(slot);

        // when
        slotService.releaseSlotByAppointment(masterId, date, time);

        // then
        assertFalse(slot.getIsBooked());
        verify(slotRepository).findByMasterIdAndSlotDateAndStartTime(masterId, date, time);
        verify(slotRepository).save(slot);
    }

    @Test
    void releaseSlotByAppointment_ShouldDoNothing_WhenSlotNotFound() {
        // given
        Long masterId = 1L;
        LocalDate date = slotDate;
        LocalTime time = startTime;

        when(slotRepository.findByMasterIdAndSlotDateAndStartTime(masterId, date, time))
                .thenReturn(Collections.emptyList());

        // when
        slotService.releaseSlotByAppointment(masterId, date, time);

        // then
        verify(slotRepository).findByMasterIdAndSlotDateAndStartTime(masterId, date, time);
        verify(slotRepository, never()).save(any());
    }

    @Test
    void releaseSlotByAppointment_ShouldUseFirstSlot_WhenMultipleSlotsFound() {
        // given
        Long masterId = 1L;
        LocalDate date = slotDate;
        LocalTime time = startTime;

        AvailabilitySlot slot2 = new AvailabilitySlot();
        slot2.setId(2L);
        slot2.setIsBooked(true);

        List<AvailabilitySlot> slots = List.of(slot, slot2);

        when(slotRepository.findByMasterIdAndSlotDateAndStartTime(masterId, date, time))
                .thenReturn(slots);
        when(slotRepository.save(slot)).thenReturn(slot);

        // when
        slotService.releaseSlotByAppointment(masterId, date, time);

        // then
        assertFalse(slot.getIsBooked());
        assertTrue(slot2.getIsBooked()); // второй не изменился
        verify(slotRepository).findByMasterIdAndSlotDateAndStartTime(masterId, date, time);
        verify(slotRepository).save(slot);
    }
}