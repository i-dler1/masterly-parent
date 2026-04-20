package com.masterly.core.controller;

import com.masterly.core.dto.ServiceEntityDto;
import com.masterly.core.dto.ServiceMaterialDto;
import com.masterly.core.mapper.ServiceEntityMapper;
import com.masterly.core.entity.Master;
import com.masterly.core.entity.ServiceEntity;
import com.masterly.core.repository.MasterRepository;
import com.masterly.core.service.ServiceEntityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceEntityControllerTest {

    @Mock
    private ServiceEntityService serviceService;

    @Mock
    private ServiceEntityMapper serviceMapper;

    @Mock
    private MasterRepository masterRepository;

    @InjectMocks
    private ServiceEntityController serviceController;

    private Master master;
    private ServiceEntity service;
    private ServiceEntityDto serviceDto;
    private Long masterId;
    private Long serviceId;

    @BeforeEach
    void setUp() {
        masterId = 1L;
        serviceId = 1L;

        master = new Master();
        master.setId(masterId);
        master.setEmail("master@test.com");

        service = new ServiceEntity();
        service.setId(serviceId);
        service.setMaster(master);
        service.setName("Тестовая услуга");
        service.setDurationMinutes(60);
        service.setPrice(BigDecimal.valueOf(1000));
        service.setIsActive(true);

        serviceDto = new ServiceEntityDto();
        serviceDto.setId(serviceId);
        serviceDto.setMasterId(masterId);
        serviceDto.setName("Тестовая услуга");
        serviceDto.setDurationMinutes(60);
        serviceDto.setPrice(BigDecimal.valueOf(1000));
        serviceDto.setIsActive(true);
    }

    @Test
    void getService_ShouldReturnServiceDto_WhenExists() {
        when(serviceService.getService(serviceId)).thenReturn(service);
        when(serviceMapper.toDto(service)).thenReturn(serviceDto);

        ResponseEntity<ServiceEntityDto> response = serviceController.getService(serviceId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(serviceId, response.getBody().getId());
        verify(serviceService).getService(serviceId);
        verify(serviceMapper).toDto(service);
    }

    @Test
    void getService_ShouldReturnNotFound_WhenNotExists() {
        when(serviceService.getService(serviceId)).thenReturn(null);

        ResponseEntity<ServiceEntityDto> response = serviceController.getService(serviceId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(serviceService).getService(serviceId);
        verify(serviceMapper, never()).toDto(any());
    }

    @Test
    void createService_ShouldCreateAndReturnServiceDto() {
        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(serviceMapper.toEntity(serviceDto)).thenReturn(service);
        when(serviceService.createService(service)).thenReturn(service);
        when(serviceMapper.toDto(service)).thenReturn(serviceDto);

        ResponseEntity<ServiceEntityDto> response = serviceController.createService(masterId, serviceDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(masterRepository).findById(masterId);
        verify(serviceMapper).toEntity(serviceDto);
        verify(serviceService).createService(service);
        verify(serviceMapper).toDto(service);
    }

    @Test
    void createService_ShouldReturnBadRequest_WhenMasterNotFound() {
        when(masterRepository.findById(masterId)).thenReturn(Optional.empty());

        ResponseEntity<ServiceEntityDto> response = serviceController.createService(masterId, serviceDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(masterRepository).findById(masterId);
        verify(serviceService, never()).createService(any());
    }

    @Test
    void updateService_ShouldUpdateAndReturnServiceDto() {
        when(serviceService.getService(serviceId)).thenReturn(service);
        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(serviceMapper.toEntity(serviceDto)).thenReturn(service);
        when(serviceService.updateService(service)).thenReturn(service);
        when(serviceMapper.toDto(service)).thenReturn(serviceDto);

        ResponseEntity<ServiceEntityDto> response = serviceController.updateService(serviceId, masterId, serviceDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(serviceService).getService(serviceId);
        verify(masterRepository).findById(masterId);
        verify(serviceMapper).toEntity(serviceDto);
        verify(serviceService).updateService(service);
        verify(serviceMapper).toDto(service);
    }

    @Test
    void updateService_ShouldReturnNotFound_WhenServiceNotExists() {
        when(serviceService.getService(serviceId)).thenReturn(null);

        ResponseEntity<ServiceEntityDto> response = serviceController.updateService(serviceId, masterId, serviceDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(serviceService).getService(serviceId);
        verify(masterRepository, never()).findById(any());
        verify(serviceService, never()).updateService(any());
    }

    @Test
    void updateService_ShouldReturnBadRequest_WhenMasterNotFound() {
        when(serviceService.getService(serviceId)).thenReturn(service);
        when(masterRepository.findById(masterId)).thenReturn(Optional.empty());

        ResponseEntity<ServiceEntityDto> response = serviceController.updateService(serviceId, masterId, serviceDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(serviceService).getService(serviceId);
        verify(masterRepository).findById(masterId);
        verify(serviceService, never()).updateService(any());
    }

    @Test
    void deleteService_ShouldDeleteService() {
        // given
        doNothing().when(serviceService).deleteService(serviceId);

        // when
        ResponseEntity<Void> response = serviceController.deleteService(serviceId);

        // then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(serviceService).deleteService(serviceId);
    }

    @Test
    void deleteService_ShouldReturnNotFound_WhenServiceNotExists() {
        // given
        doThrow(new RuntimeException("Service not found")).when(serviceService).deleteService(serviceId);

        // when & then
        assertThrows(RuntimeException.class, () -> serviceController.deleteService(serviceId));
    }

    @Test
    void getServicesPaginated_ShouldReturnPageOfServiceDto() {
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "asc";
        Page<ServiceEntityDto> servicePage = new PageImpl<>(Collections.singletonList(serviceDto));

        when(serviceService.getServicesByMasterId(eq(masterId), any(Pageable.class))).thenReturn(servicePage);

        ResponseEntity<Page<ServiceEntityDto>> response = serviceController.getServicesPaginated(page, size, sortBy, sortDir, masterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(serviceService).getServicesByMasterId(eq(masterId), any(Pageable.class));
    }

    @Test
    void getServicesPaginated_WithDescSort_ShouldReturnPageOfServiceDto() {
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "desc";
        Page<ServiceEntityDto> servicePage = new PageImpl<>(Collections.singletonList(serviceDto));

        when(serviceService.getServicesByMasterId(eq(masterId), any(Pageable.class))).thenReturn(servicePage);

        ResponseEntity<Page<ServiceEntityDto>> response = serviceController.getServicesPaginated(page, size, sortBy, sortDir, masterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(serviceService).getServicesByMasterId(eq(masterId), any(Pageable.class));
    }

    @Test
    void getAllServices_ShouldReturnListOfServiceDto() {
        when(masterRepository.existsById(masterId)).thenReturn(true);
        List<ServiceEntity> services = Collections.singletonList(service);
        when(serviceService.getAllServices(masterId)).thenReturn(services);
        when(serviceMapper.toDto(service)).thenReturn(serviceDto);

        ResponseEntity<List<ServiceEntityDto>> response = serviceController.getAllServices(masterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(masterRepository).existsById(masterId);
        verify(serviceService).getAllServices(masterId);
        verify(serviceMapper).toDto(service);
    }

    @Test
    void getAllServices_ShouldReturnEmptyList_WhenNoServices() {
        when(masterRepository.existsById(masterId)).thenReturn(true);
        when(serviceService.getAllServices(masterId)).thenReturn(Collections.emptyList());

        ResponseEntity<List<ServiceEntityDto>> response = serviceController.getAllServices(masterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(masterRepository).existsById(masterId);
        verify(serviceService).getAllServices(masterId);
        verify(serviceMapper, never()).toDto(any());
    }

    @Test
    void getAllServices_ShouldReturnBadRequest_WhenMasterNotFound() {
        when(masterRepository.existsById(masterId)).thenReturn(false);

        ResponseEntity<List<ServiceEntityDto>> response = serviceController.getAllServices(masterId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(masterRepository).existsById(masterId);
        verify(serviceService, never()).getAllServices(any());
    }

    @Test
    void getServicesByMasterId_ShouldReturnListOfServiceDto() {
        when(masterRepository.existsById(masterId)).thenReturn(true);
        List<ServiceEntityDto> services = Collections.singletonList(serviceDto);
        when(serviceService.getServicesByMasterId(masterId)).thenReturn(services);

        ResponseEntity<List<ServiceEntityDto>> response = serviceController.getServicesByMasterId(masterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(masterRepository).existsById(masterId);
        verify(serviceService).getServicesByMasterId(masterId);
    }

    @Test
    void getServicesByMasterId_ShouldReturnEmptyList_WhenNoServices() {
        when(masterRepository.existsById(masterId)).thenReturn(true);
        when(serviceService.getServicesByMasterId(masterId)).thenReturn(Collections.emptyList());

        ResponseEntity<List<ServiceEntityDto>> response = serviceController.getServicesByMasterId(masterId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(masterRepository).existsById(masterId);
        verify(serviceService).getServicesByMasterId(masterId);
    }

    @Test
    void getServicesByMasterId_ShouldReturnBadRequest_WhenMasterNotFound() {
        when(masterRepository.existsById(masterId)).thenReturn(false);

        ResponseEntity<List<ServiceEntityDto>> response = serviceController.getServicesByMasterId(masterId);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(masterRepository).existsById(masterId);
        verify(serviceService, never()).getServicesByMasterId(any());
    }

    @Test
    void getAllServicesForAdmin_ShouldReturnPageOfServiceDto() {
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "asc";
        Page<ServiceEntityDto> servicePage = new PageImpl<>(Collections.singletonList(serviceDto));

        when(serviceService.getServicesForAdmin(any(Pageable.class))).thenReturn(servicePage);

        ResponseEntity<Page<ServiceEntityDto>> response = serviceController.getAllServicesForAdmin(page, size, sortBy, sortDir);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(serviceService).getServicesForAdmin(any(Pageable.class));
    }

    @Test
    void getAllServicesForAdmin_WithDescSort_ShouldReturnPageOfServiceDto() {
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "desc";
        Page<ServiceEntityDto> servicePage = new PageImpl<>(Collections.singletonList(serviceDto));

        when(serviceService.getServicesForAdmin(any(Pageable.class))).thenReturn(servicePage);

        ResponseEntity<Page<ServiceEntityDto>> response = serviceController.getAllServicesForAdmin(page, size, sortBy, sortDir);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(serviceService).getServicesForAdmin(any(Pageable.class));
    }

    // ==================== deactivateService ====================

    @Test
    void deactivateService_ShouldReturnOk() {
        // given
        doNothing().when(serviceService).deactivateService(serviceId);

        // when
        ResponseEntity<Void> response = serviceController.deactivateService(serviceId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(serviceService).deactivateService(serviceId);
    }

// ==================== activateService ====================

    @Test
    void activateService_ShouldReturnOk_WhenServiceExists() {
        // given
        when(serviceService.getService(serviceId)).thenReturn(service);
        when(serviceService.updateService(service)).thenReturn(service);

        // when
        ResponseEntity<Void> response = serviceController.activateService(serviceId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(service.getIsActive());
        verify(serviceService).getService(serviceId);
        verify(serviceService).updateService(service);
    }

    @Test
    void activateService_ShouldReturnNotFound_WhenServiceNotExists() {
        // given
        when(serviceService.getService(serviceId)).thenReturn(null);

        // when
        ResponseEntity<Void> response = serviceController.activateService(serviceId);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(serviceService).getService(serviceId);
        verify(serviceService, never()).updateService(any());
    }

// ==================== deleteService - conflict ====================

    @Test
    void deleteService_ShouldReturnConflict_WhenServiceHasAppointments() {
        // given
        doThrow(new RuntimeException("Service has existing appointments"))
                .when(serviceService).deleteService(serviceId);

        // when
        ResponseEntity<Void> response = serviceController.deleteService(serviceId);

        // then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(serviceService).deleteService(serviceId);
    }

// ==================== getServiceMaterials ====================

    @Test
    void getServiceMaterials_ShouldReturnListOfServiceMaterialDto() {
        // given
        ServiceMaterialDto materialDto = ServiceMaterialDto.builder()
                .id(1L)
                .serviceId(serviceId)
                .materialId(1L)
                .materialName("Краска")
                .quantityUsed(BigDecimal.valueOf(50))
                .build();
        List<ServiceMaterialDto> materials = Collections.singletonList(materialDto);

        when(serviceService.getMaterialsByServiceId(serviceId)).thenReturn(materials);

        // when
        ResponseEntity<List<ServiceMaterialDto>> response = serviceController.getServiceMaterials(serviceId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(serviceService).getMaterialsByServiceId(serviceId);
    }

// ==================== addMaterialToService ====================

    @Test
    void addMaterialToService_ShouldReturnOk() {
        // given
        Long materialId = 1L;
        BigDecimal quantityUsed = BigDecimal.valueOf(50);
        String notes = "Тестовое примечание";

        doNothing().when(serviceService).addMaterialToService(serviceId, materialId, quantityUsed, notes);

        // when
        ResponseEntity<Void> response = serviceController.addMaterialToService(serviceId, materialId, quantityUsed, notes);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(serviceService).addMaterialToService(serviceId, materialId, quantityUsed, notes);
    }

// ==================== removeMaterialFromService ====================

    @Test
    void removeMaterialFromService_ShouldReturnOk() {
        // given
        Long serviceMaterialId = 1L;
        doNothing().when(serviceService).removeMaterialFromService(serviceMaterialId);

        // when
        ResponseEntity<Void> response = serviceController.removeMaterialFromService(serviceMaterialId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(serviceService).removeMaterialFromService(serviceMaterialId);
    }
}