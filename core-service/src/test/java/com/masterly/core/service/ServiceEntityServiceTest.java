package com.masterly.core.service;

import com.masterly.core.dto.ServiceEntityDto;
import com.masterly.core.dto.ServiceMaterialDto;
import com.masterly.core.mapper.ServiceEntityMapper;
import com.masterly.core.entity.Appointment;
import com.masterly.core.entity.Master;
import com.masterly.core.entity.Material;
import com.masterly.core.entity.ServiceEntity;
import com.masterly.core.entity.ServiceMaterial;
import com.masterly.core.repository.AppointmentRepository;
import com.masterly.core.repository.MaterialRepository;
import com.masterly.core.repository.ServiceEntityRepository;
import com.masterly.core.repository.ServiceMaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceEntityServiceTest {

    @Mock
    private ServiceEntityRepository serviceRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ServiceMaterialRepository serviceMaterialRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private ServiceEntityMapper serviceMapper;

    @InjectMocks
    private ServiceEntityService serviceEntityService;

    private Master master;
    private ServiceEntity service;
    private ServiceEntityDto serviceDto;
    private Long masterId;

    @BeforeEach
    void setUp() {
        masterId = 1L;

        master = new Master();
        master.setId(masterId);
        master.setEmail("master@test.com");

        service = new ServiceEntity();
        service.setId(1L);
        service.setMaster(master);
        service.setName("Тестовая услуга");
        service.setDurationMinutes(60);
        service.setPrice(BigDecimal.valueOf(1000));
        service.setIsActive(true);

        serviceDto = new ServiceEntityDto();
        serviceDto.setId(1L);
        serviceDto.setMasterId(masterId);
        serviceDto.setName("Тестовая услуга");
        serviceDto.setDurationMinutes(60);
        serviceDto.setPrice(BigDecimal.valueOf(1000));
        serviceDto.setIsActive(true);
    }

    // ==================== getAllServices ====================

    @Test
    void getAllServices_ShouldReturnListOfServices() {
        List<ServiceEntity> services = Collections.singletonList(service);
        when(serviceRepository.findByMasterId(masterId)).thenReturn(services);

        List<ServiceEntity> result = serviceEntityService.getAllServices(masterId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(serviceRepository).findByMasterId(masterId);
    }

    @Test
    void getAllServices_ShouldReturnEmptyList_WhenNoServices() {
        when(serviceRepository.findByMasterId(masterId)).thenReturn(Collections.emptyList());

        List<ServiceEntity> result = serviceEntityService.getAllServices(masterId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(serviceRepository).findByMasterId(masterId);
    }

    // ==================== getService ====================

    @Test
    void getService_ShouldReturnService_WhenExists() {
        Long id = 1L;
        when(serviceRepository.findById(id)).thenReturn(Optional.of(service));

        ServiceEntity result = serviceEntityService.getService(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(serviceRepository).findById(id);
    }

    @Test
    void getService_ShouldReturnNull_WhenNotExists() {
        Long id = 999L;
        when(serviceRepository.findById(id)).thenReturn(Optional.empty());

        ServiceEntity result = serviceEntityService.getService(id);

        assertNull(result);
        verify(serviceRepository).findById(id);
    }

    // ==================== createService ====================

    @Test
    void createService_ShouldCreateAndReturnService() {
        when(serviceRepository.save(any(ServiceEntity.class))).thenReturn(service);

        ServiceEntity result = serviceEntityService.createService(service);

        assertNotNull(result);
        verify(serviceRepository).save(service);
    }

    // ==================== updateService ====================

    @Test
    void updateService_ShouldUpdateAndReturnService() {
        when(serviceRepository.save(any(ServiceEntity.class))).thenReturn(service);

        ServiceEntity result = serviceEntityService.updateService(service);

        assertNotNull(result);
        verify(serviceRepository).save(service);
    }

    // ==================== deleteService ====================

    @Test
    void deleteService_ShouldDeleteService_WhenNoAppointments() {
        Long id = 1L;
        when(appointmentRepository.findByServiceId(id)).thenReturn(Collections.emptyList());
        doNothing().when(serviceRepository).deleteById(id);

        serviceEntityService.deleteService(id);

        verify(appointmentRepository).findByServiceId(id);
        verify(serviceRepository).deleteById(id);
    }

    @Test
    void deleteService_ShouldThrowException_WhenHasAppointments() {
        Long id = 1L;
        List<Appointment> appointments = Collections.singletonList(new Appointment());
        when(appointmentRepository.findByServiceId(id)).thenReturn(appointments);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            serviceEntityService.deleteService(id);
        });

        assertEquals("Service has existing appointments", exception.getMessage());
        verify(appointmentRepository).findByServiceId(id);
        verify(serviceRepository, never()).deleteById(any());
    }

    // ==================== deactivateService ====================

    @Test
    void deactivateService_ShouldSetIsActiveToFalse() {
        Long id = 1L;
        service.setIsActive(true);
        when(serviceRepository.findById(id)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(ServiceEntity.class))).thenReturn(service);

        serviceEntityService.deactivateService(id);

        assertFalse(service.getIsActive());
        verify(serviceRepository).findById(id);
        verify(serviceRepository).save(service);
    }

    @Test
    void deactivateService_ShouldThrowException_WhenServiceNotFound() {
        Long id = 999L;
        when(serviceRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            serviceEntityService.deactivateService(id);
        });

        assertEquals("Service not found", exception.getMessage());
        verify(serviceRepository).findById(id);
        verify(serviceRepository, never()).save(any());
    }

    // ==================== activateService ====================

    @Test
    void activateService_ShouldSetIsActiveToTrue() {
        Long id = 1L;
        service.setIsActive(false);
        when(serviceRepository.findById(id)).thenReturn(Optional.of(service));
        when(serviceRepository.save(any(ServiceEntity.class))).thenReturn(service);

        serviceEntityService.activateService(id);

        assertTrue(service.getIsActive());
        verify(serviceRepository).findById(id);
        verify(serviceRepository).save(service);
    }

    @Test
    void activateService_ShouldThrowException_WhenServiceNotFound() {
        Long id = 999L;
        when(serviceRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            serviceEntityService.activateService(id);
        });

        assertEquals("Service not found", exception.getMessage());
        verify(serviceRepository).findById(id);
        verify(serviceRepository, never()).save(any());
    }

    // ==================== getServicesByMasterId (Page) ====================

    @Test
    void getServicesByMasterId_ShouldReturnPageOfServiceDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceEntity> servicePage = new PageImpl<>(Collections.singletonList(service));

        when(serviceRepository.findByMasterId(masterId, pageable)).thenReturn(servicePage);
        when(serviceMapper.toDto(service)).thenReturn(serviceDto);

        Page<ServiceEntityDto> result = serviceEntityService.getServicesByMasterId(masterId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(serviceRepository).findByMasterId(masterId, pageable);
        verify(serviceMapper).toDto(service);
    }

    // ==================== getServicesByMasterId (List) ====================

    @Test
    void getServicesByMasterIdAsList_ShouldReturnListOfServiceDto() {
        List<ServiceEntity> services = Collections.singletonList(service);
        when(serviceRepository.findByMasterId(masterId)).thenReturn(services);
        when(serviceMapper.toDto(service)).thenReturn(serviceDto);

        List<ServiceEntityDto> result = serviceEntityService.getServicesByMasterId(masterId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(serviceRepository).findByMasterId(masterId);
        verify(serviceMapper).toDto(service);
    }

    // ==================== getServicesForAdmin ====================

    @Test
    void getServicesForAdmin_ShouldReturnPageOfServiceDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ServiceEntity> servicePage = new PageImpl<>(Collections.singletonList(service));

        when(serviceRepository.findAll(pageable)).thenReturn(servicePage);
        when(serviceMapper.toDto(service)).thenReturn(serviceDto);

        Page<ServiceEntityDto> result = serviceEntityService.getServicesForAdmin(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(serviceRepository).findAll(pageable);
        verify(serviceMapper).toDto(service);
    }

    // ==================== getAllServicesByMasterId ====================

    @Test
    void getAllServicesByMasterId_ShouldReturnListOfServiceDto() {
        List<ServiceEntity> services = Collections.singletonList(service);
        when(serviceRepository.findByMasterId(masterId)).thenReturn(services);
        when(serviceMapper.toDtoList(services)).thenReturn(Collections.singletonList(serviceDto));

        List<ServiceEntityDto> result = serviceEntityService.getAllServicesByMasterId(masterId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(serviceRepository).findByMasterId(masterId);
        verify(serviceMapper).toDtoList(services);
    }

    // ==================== getMaterialsByServiceId ====================

    @Test
    void getMaterialsByServiceId_ShouldReturnListOfServiceMaterialDto() {
        Long serviceId = 1L;
        ServiceMaterial serviceMaterial = new ServiceMaterial();
        serviceMaterial.setId(1L);
        serviceMaterial.setService(service);
        Material material = new Material();
        material.setId(1L);
        material.setName("Краска");
        serviceMaterial.setMaterial(material);
        serviceMaterial.setQuantityUsed(BigDecimal.valueOf(50));

        when(serviceMaterialRepository.findByServiceId(serviceId))
                .thenReturn(Collections.singletonList(serviceMaterial));

        List<ServiceMaterialDto> result = serviceEntityService.getMaterialsByServiceId(serviceId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(serviceId, result.get(0).getServiceId());
        assertEquals(1L, result.get(0).getMaterialId());
        assertEquals("Краска", result.get(0).getMaterialName());
        assertEquals(BigDecimal.valueOf(50), result.get(0).getQuantityUsed());
        verify(serviceMaterialRepository).findByServiceId(serviceId);
    }

    // ==================== addMaterialToService ====================

    @Test
    void addMaterialToService_ShouldSaveServiceMaterial() {
        Long serviceId = 1L;
        Long materialId = 1L;
        BigDecimal quantityUsed = BigDecimal.valueOf(50);
        String notes = "Тестовое примечание";

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        Material material = new Material();
        material.setId(materialId);
        material.setName("Краска");
        when(materialRepository.findById(materialId)).thenReturn(Optional.of(material));
        when(serviceMaterialRepository.save(any(ServiceMaterial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        serviceEntityService.addMaterialToService(serviceId, materialId, quantityUsed, notes);

        verify(serviceRepository).findById(serviceId);
        verify(materialRepository).findById(materialId);
        ArgumentCaptor<ServiceMaterial> captor = ArgumentCaptor.forClass(ServiceMaterial.class);
        verify(serviceMaterialRepository).save(captor.capture());
        ServiceMaterial saved = captor.getValue();
        assertEquals(service, saved.getService());
        assertEquals(material, saved.getMaterial());
        assertEquals(quantityUsed, saved.getQuantityUsed());
        assertEquals(notes, saved.getNotes());
    }

    @Test
    void addMaterialToService_ShouldThrowException_WhenServiceNotFound() {
        Long serviceId = 999L;
        Long materialId = 1L;
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            serviceEntityService.addMaterialToService(serviceId, materialId, BigDecimal.TEN, null);
        });

        assertEquals("Service not found", exception.getMessage());
        verify(serviceRepository).findById(serviceId);
        verify(materialRepository, never()).findById(any());
        verify(serviceMaterialRepository, never()).save(any());
    }

    @Test
    void addMaterialToService_ShouldThrowException_WhenMaterialNotFound() {
        Long serviceId = 1L;
        Long materialId = 999L;
        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(service));
        when(materialRepository.findById(materialId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            serviceEntityService.addMaterialToService(serviceId, materialId, BigDecimal.TEN, null);
        });

        assertEquals("Material not found", exception.getMessage());
        verify(serviceRepository).findById(serviceId);
        verify(materialRepository).findById(materialId);
        verify(serviceMaterialRepository, never()).save(any());
    }

    // ==================== removeMaterialFromService ====================

    @Test
    void removeMaterialFromService_ShouldDeleteServiceMaterial() {
        Long serviceMaterialId = 1L;
        doNothing().when(serviceMaterialRepository).deleteById(serviceMaterialId);

        serviceEntityService.removeMaterialFromService(serviceMaterialId);

        verify(serviceMaterialRepository).deleteById(serviceMaterialId);
    }
}