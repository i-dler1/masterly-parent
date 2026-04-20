package com.masterly.core.service;

import com.masterly.core.dto.MaterialDto;
import com.masterly.core.mapper.MaterialMapper;
import com.masterly.core.entity.Master;
import com.masterly.core.entity.Material;
import com.masterly.core.entity.ServiceMaterial;
import com.masterly.core.repository.MaterialRepository;
import com.masterly.core.repository.ServiceMaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private ServiceMaterialRepository serviceMaterialRepository;

    @Mock
    private MaterialMapper materialMapper;

    @InjectMocks
    private MaterialService materialService;

    private Master master;
    private Material material;
    private MaterialDto materialDto;
    private Long masterId;

    @BeforeEach
    void setUp() {
        masterId = 1L;

        master = new Master();
        master.setId(masterId);
        master.setEmail("master@test.com");

        material = new Material();
        material.setId(1L);
        material.setMaster(master);
        material.setName("Тестовый материал");
        material.setUnit("шт");
        material.setQuantity(BigDecimal.valueOf(100));
        material.setMinQuantity(BigDecimal.valueOf(10));
        material.setPricePerUnit(BigDecimal.valueOf(500));
        material.setSupplier("Поставщик");
        material.setNotes("Примечание");

        materialDto = new MaterialDto();
        materialDto.setId(1L);
        materialDto.setMasterId(masterId);
        materialDto.setName("Тестовый материал");
        materialDto.setUnit("шт");
        materialDto.setQuantity(BigDecimal.valueOf(100));
        materialDto.setMinQuantity(BigDecimal.valueOf(10));
        materialDto.setPricePerUnit(BigDecimal.valueOf(500));
        materialDto.setSupplier("Поставщик");
        materialDto.setNotes("Примечание");
    }

    // ==================== getAllMaterials ====================

    @Test
    void getAllMaterials_ShouldReturnListOfMaterials() {
        // given
        List<Material> materials = Collections.singletonList(material);
        when(materialRepository.findByMasterId(masterId)).thenReturn(materials);

        // when
        List<Material> result = materialService.getAllMaterials(masterId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(materialRepository).findByMasterId(masterId);
    }

    @Test
    void getAllMaterials_ShouldReturnEmptyList_WhenNoMaterials() {
        // given
        when(materialRepository.findByMasterId(masterId)).thenReturn(Collections.emptyList());

        // when
        List<Material> result = materialService.getAllMaterials(masterId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(materialRepository).findByMasterId(masterId);
    }

    // ==================== getMaterial ====================

    @Test
    void getMaterial_ShouldReturnMaterial_WhenExists() {
        // given
        Long id = 1L;
        when(materialRepository.findById(id)).thenReturn(Optional.of(material));

        // when
        Material result = materialService.getMaterial(id);

        // then
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(materialRepository).findById(id);
    }

    @Test
    void getMaterial_ShouldReturnNull_WhenNotExists() {
        // given
        Long id = 999L;
        when(materialRepository.findById(id)).thenReturn(Optional.empty());

        // when
        Material result = materialService.getMaterial(id);

        // then
        assertNull(result);
        verify(materialRepository).findById(id);
    }

    // ==================== createMaterial ====================

    @Test
    void createMaterial_ShouldCreateAndReturnMaterial() {
        // given
        when(materialRepository.save(any(Material.class))).thenReturn(material);

        // when
        Material result = materialService.createMaterial(material);

        // then
        assertNotNull(result);
        verify(materialRepository).save(material);
    }

    // ==================== updateMaterial ====================

    @Test
    void updateMaterial_ShouldUpdateAndReturnMaterial() {
        // given
        when(materialRepository.save(any(Material.class))).thenReturn(material);

        // when
        Material result = materialService.updateMaterial(material);

        // then
        assertNotNull(result);
        verify(materialRepository).save(material);
    }

    // ==================== deleteMaterial ====================

    @Test
    void deleteMaterial_ShouldDeleteMaterial_WhenNotUsedInServices() {
        // given
        Long id = 1L;
        when(serviceMaterialRepository.findByMaterialId(id)).thenReturn(Collections.emptyList());
        doNothing().when(materialRepository).deleteById(id);

        // when
        materialService.deleteMaterial(id);

        // then
        verify(serviceMaterialRepository).findByMaterialId(id);
        verify(materialRepository).deleteById(id);
    }

    @Test
    void deleteMaterial_ShouldThrowException_WhenUsedInServices() {
        // given
        Long id = 1L;
        List<ServiceMaterial> serviceMaterials = Collections.singletonList(new ServiceMaterial());
        when(serviceMaterialRepository.findByMaterialId(id)).thenReturn(serviceMaterials);

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            materialService.deleteMaterial(id);
        });

        assertEquals("Нельзя удалить материал, так как он используется в услугах", exception.getMessage());
        verify(serviceMaterialRepository).findByMaterialId(id);
        verify(materialRepository, never()).deleteById(any());
    }

    // ==================== getMaterialsByMasterId (Page) ====================

    @Test
    void getMaterialsByMasterId_ShouldReturnPageOfMaterialDto() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Material> materialPage = new PageImpl<>(Collections.singletonList(material));

        when(materialRepository.findByMasterId(masterId, pageable)).thenReturn(materialPage);
        when(materialMapper.toDto(material)).thenReturn(materialDto);

        // when
        Page<MaterialDto> result = materialService.getMaterialsByMasterId(masterId, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(materialRepository).findByMasterId(masterId, pageable);
        verify(materialMapper).toDto(material);
    }

    @Test
    void getMaterialsByMasterId_ShouldReturnEmptyPage_WhenNoMaterials() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Material> emptyPage = new PageImpl<>(Collections.emptyList());

        when(materialRepository.findByMasterId(masterId, pageable)).thenReturn(emptyPage);

        // when
        Page<MaterialDto> result = materialService.getMaterialsByMasterId(masterId, pageable);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(materialRepository).findByMasterId(masterId, pageable);
        verify(materialMapper, never()).toDto(any());
    }

    // ==================== getMaterialsByMasterId (List) ====================



    // ==================== getMaterialsForAdmin ====================

    @Test
    void getMaterialsForAdmin_ShouldReturnPageOfMaterialDto() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Material> materialPage = new PageImpl<>(Collections.singletonList(material));

        when(materialRepository.findAll(pageable)).thenReturn(materialPage);
        when(materialMapper.toDto(material)).thenReturn(materialDto);

        // when
        Page<MaterialDto> result = materialService.getMaterialsForAdmin(pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(materialRepository).findAll(pageable);
        verify(materialMapper).toDto(material);
    }

    // ==================== getAllMaterialsByMasterId ====================

    @Test
    void getAllMaterialsByMasterId_ShouldReturnListOfMaterialDto() {
        // given
        List<Material> materials = Collections.singletonList(material);
        when(materialRepository.findByMasterId(masterId)).thenReturn(materials);
        when(materialMapper.toDto(material)).thenReturn(materialDto);

        // when
        List<MaterialDto> result = materialService.getAllMaterialsByMasterId(masterId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(materialDto, result.get(0));
        verify(materialRepository).findByMasterId(masterId);
        verify(materialMapper).toDto(material);
    }

    @Test
    void getAllMaterialsByMasterId_ShouldReturnEmptyList_WhenNoMaterials() {
        // given
        when(materialRepository.findByMasterId(masterId)).thenReturn(Collections.emptyList());

        // when
        List<MaterialDto> result = materialService.getAllMaterialsByMasterId(masterId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(materialRepository).findByMasterId(masterId);
        verify(materialMapper, never()).toDto(any());
    }
}