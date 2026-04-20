package com.masterly.core.controller;

import com.masterly.core.dto.MasterCreateDto;
import com.masterly.core.dto.MasterDto;
import com.masterly.core.dto.MasterUpdateDto;
import com.masterly.core.service.MasterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MasterControllerTest {

    @Mock
    private MasterService masterService;

    @InjectMocks
    private MasterController masterController;

    private MasterDto masterDto;
    private MasterCreateDto createDto;
    private MasterUpdateDto updateDto;
    private Long masterId;

    @BeforeEach
    void setUp() {
        masterId = 1L;

        masterDto = new MasterDto();
        masterDto.setId(masterId);
        masterDto.setEmail("test@masterly.com");
        masterDto.setFullName("Тестовый Мастер");
        masterDto.setRole("MASTER");
        masterDto.setIsActive(true);

        createDto = new MasterCreateDto();
        createDto.setEmail("new@masterly.com");
        createDto.setPassword("123");
        createDto.setFullName("Новый Мастер");

        updateDto = new MasterUpdateDto();
        updateDto.setFullName("Обновлённый Мастер");
        updateDto.setPhone("+375291234567");
        updateDto.setSpecialization("Специалист");
    }

    @Test
    void register_ShouldReturnCreatedMaster() {
        when(masterService.register(any(MasterCreateDto.class))).thenReturn(masterDto);

        ResponseEntity<MasterDto> response = masterController.register(createDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(masterService).register(createDto);
    }

    @Test
    void getProfile_ShouldReturnMasterProfile() {
        when(masterService.getMasterById(masterId)).thenReturn(masterDto);

        ResponseEntity<MasterDto> response = masterController.getProfile(masterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(masterService).getMasterById(masterId);
    }

    @Test
    void updateProfile_ShouldUpdateAndReturnMaster() {
        when(masterService.updateMaster(eq(masterId), any(MasterUpdateDto.class))).thenReturn(masterDto);

        ResponseEntity<MasterDto> response = masterController.updateProfile(masterId, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(masterService).updateMaster(eq(masterId), any(MasterUpdateDto.class));
    }

    @Test
    void getAllMasters_ShouldReturnListOfMasters() {
        List<MasterDto> masters = Collections.singletonList(masterDto);
        when(masterService.getAllMasters()).thenReturn(masters);

        ResponseEntity<List<MasterDto>> response = masterController.getAllMasters();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(masterService).getAllMasters();
    }

    @Test
    void getMasterById_ShouldReturnMaster() {
        when(masterService.getMasterById(masterId)).thenReturn(masterDto);

        ResponseEntity<MasterDto> response = masterController.getMasterById(masterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(masterService).getMasterById(masterId);
    }

    @Test
    void getMasterByEmail_ShouldReturnMaster() {
        String email = "test@masterly.com";
        when(masterService.findByEmail(email)).thenReturn(masterDto);

        ResponseEntity<MasterDto> response = masterController.getMasterByEmail(email);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(masterService).findByEmail(email);
    }
}