package com.masterly.core.controller;

import com.masterly.core.dto.MaterialDto;
import com.masterly.core.mapper.MaterialMapper;
import com.masterly.core.entity.Master;
import com.masterly.core.entity.Material;
import com.masterly.core.repository.MasterRepository;
import com.masterly.core.service.MaterialService;
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
class MaterialControllerTest {

    @Mock
    private MaterialService materialService;

    @Mock
    private MaterialMapper materialMapper;

    @Mock
    private MasterRepository masterRepository;

    @InjectMocks
    private MaterialController materialController;

    private Master master;
    private Material material;
    private MaterialDto materialDto;
    private Long masterId;
    private Long materialId;

    @BeforeEach
    void setUp() {
        masterId = 1L;
        materialId = 1L;

        master = new Master();
        master.setId(masterId);
        master.setEmail("master@test.com");

        material = new Material();
        material.setId(materialId);
        material.setMaster(master);
        material.setName("Тестовый материал");
        material.setUnit("шт");
        material.setQuantity(BigDecimal.valueOf(100));
        material.setPricePerUnit(BigDecimal.valueOf(500));

        materialDto = new MaterialDto();
        materialDto.setId(materialId);
        materialDto.setMasterId(masterId);
        materialDto.setName("Тестовый материал");
        materialDto.setUnit("шт");
        materialDto.setQuantity(BigDecimal.valueOf(100));
        materialDto.setPricePerUnit(BigDecimal.valueOf(500));
    }

    // ==================== getMaterials ====================

    @Test
    void getMaterials_ShouldReturnListOfMaterialDto() {
        // given
        List<Material> materials = Collections.singletonList(material);
        when(materialService.getAllMaterials(masterId)).thenReturn(materials);
        when(materialMapper.toDto(material)).thenReturn(materialDto);

        // when
        ResponseEntity<List<MaterialDto>> response = materialController.getMaterials(masterId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(materialService).getAllMaterials(masterId);
        verify(materialMapper).toDto(material);
    }

    @Test
    void getMaterials_ShouldReturnEmptyList_WhenNoMaterials() {
        // given
        when(materialService.getAllMaterials(masterId)).thenReturn(Collections.emptyList());

        // when
        ResponseEntity<List<MaterialDto>> response = materialController.getMaterials(masterId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(materialService).getAllMaterials(masterId);
        verify(materialMapper, never()).toDto(any());
    }

    // ==================== getMaterial ====================

    @Test
    void getMaterial_ShouldReturnMaterialDto_WhenExists() {
        // given
        when(materialService.getMaterial(materialId)).thenReturn(material);
        when(materialMapper.toDto(material)).thenReturn(materialDto);

        // when
        ResponseEntity<MaterialDto> response = materialController.getMaterial(materialId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(materialId, response.getBody().getId());
        verify(materialService).getMaterial(materialId);
        verify(materialMapper).toDto(material);
    }

    @Test
    void getMaterial_ShouldReturnNotFound_WhenNotExists() {
        // given
        when(materialService.getMaterial(materialId)).thenReturn(null);

        // when
        ResponseEntity<MaterialDto> response = materialController.getMaterial(materialId);

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(materialService).getMaterial(materialId);
        verify(materialMapper, never()).toDto(any());
    }

    // ==================== createMaterial ====================

    @Test
    void createMaterial_ShouldCreateAndReturnMaterialDto() {
        // given
        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(materialMapper.toEntity(materialDto)).thenReturn(material);
        when(materialService.createMaterial(material)).thenReturn(material);
        when(materialMapper.toDto(material)).thenReturn(materialDto);

        // when
        ResponseEntity<MaterialDto> response = materialController.createMaterial(masterId, materialDto);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(masterRepository).findById(masterId);
        verify(materialMapper).toEntity(materialDto);
        verify(materialService).createMaterial(material);
        verify(materialMapper).toDto(material);
    }

    @Test
    void createMaterial_ShouldReturnBadRequest_WhenMasterNotFound() {
        // given
        when(masterRepository.findById(masterId)).thenReturn(Optional.empty());

        // when
        ResponseEntity<MaterialDto> response = materialController.createMaterial(masterId, materialDto);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(masterRepository).findById(masterId);
        verify(materialService, never()).createMaterial(any());
    }

    // ==================== updateMaterial ====================

    @Test
    void updateMaterial_ShouldUpdateAndReturnMaterialDto() {
        // given
        when(masterRepository.findById(masterId)).thenReturn(Optional.of(master));
        when(materialMapper.toEntity(materialDto)).thenReturn(material);
        when(materialService.updateMaterial(material)).thenReturn(material);
        when(materialMapper.toDto(material)).thenReturn(materialDto);

        // when
        ResponseEntity<MaterialDto> response = materialController.updateMaterial(materialId, masterId, materialDto);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(masterRepository).findById(masterId);
        verify(materialMapper).toEntity(materialDto);
        verify(materialService).updateMaterial(material);
        verify(materialMapper).toDto(material);
    }

    @Test
    void updateMaterial_ShouldReturnBadRequest_WhenMasterNotFound() {
        // given
        when(masterRepository.findById(masterId)).thenReturn(Optional.empty());

        // when
        ResponseEntity<MaterialDto> response = materialController.updateMaterial(materialId, masterId, materialDto);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(masterRepository).findById(masterId);
        verify(materialService, never()).updateMaterial(any());
    }

    // ==================== deleteMaterial ====================

    @Test
    void deleteMaterial_ShouldDeleteMaterial() {
        // given
        doNothing().when(materialService).deleteMaterial(materialId);

        // when
        ResponseEntity<Void> response = materialController.deleteMaterial(materialId);

        // then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(materialService).deleteMaterial(materialId);
    }

    // ==================== getMaterialsPaginated ====================

    @Test
    void getMaterialsPaginated_ShouldReturnPageOfMaterialDto() {
        // given
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "asc";
        Page<MaterialDto> materialPage = new PageImpl<>(Collections.singletonList(materialDto));

        when(materialService.getMaterialsByMasterId(eq(masterId), any(Pageable.class))).thenReturn(materialPage);

        // when
        ResponseEntity<Page<MaterialDto>> response = materialController.getMaterialsPaginated(page, size, sortBy, sortDir, masterId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(materialService).getMaterialsByMasterId(eq(masterId), any(Pageable.class));
    }

    @Test
    void getMaterialsPaginated_WithDescSort_ShouldReturnPageOfMaterialDto() {
        // given
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "desc";
        Page<MaterialDto> materialPage = new PageImpl<>(Collections.singletonList(materialDto));

        when(materialService.getMaterialsByMasterId(eq(masterId), any(Pageable.class))).thenReturn(materialPage);

        // when
        ResponseEntity<Page<MaterialDto>> response = materialController.getMaterialsPaginated(page, size, sortBy, sortDir, masterId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(materialService).getMaterialsByMasterId(eq(masterId), any(Pageable.class));
    }

    // ==================== getAllMaterialsForAdmin ====================

    @Test
    void getAllMaterialsForAdmin_ShouldReturnPageOfMaterialDto() {
        // given
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "asc";
        Page<MaterialDto> materialPage = new PageImpl<>(Collections.singletonList(materialDto));

        when(materialService.getMaterialsForAdmin(any(Pageable.class))).thenReturn(materialPage);

        // when
        ResponseEntity<Page<MaterialDto>> response = materialController.getAllMaterialsForAdmin(page, size, sortBy, sortDir);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        verify(materialService).getMaterialsForAdmin(any(Pageable.class));
    }

    @Test
    void getAllMaterialsForAdmin_WithDescSort_ShouldReturnPageOfMaterialDto() {
        // given
        int page = 0;
        int size = 10;
        String sortBy = "id";
        String sortDir = "desc";
        Page<MaterialDto> materialPage = new PageImpl<>(Collections.singletonList(materialDto));

        when(materialService.getMaterialsForAdmin(any(Pageable.class))).thenReturn(materialPage);

        // when
        ResponseEntity<Page<MaterialDto>> response = materialController.getAllMaterialsForAdmin(page, size, sortBy, sortDir);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(materialService).getMaterialsForAdmin(any(Pageable.class));
    }

    // ==================== getAllMaterials (alternative endpoint) ====================

    @Test
    void getAllMaterials_ShouldReturnListOfMaterialDto() {
        // given
        List<MaterialDto> materials = Collections.singletonList(materialDto);
        when(materialService.getAllMaterialsByMasterId(masterId)).thenReturn(materials);

        // when
        ResponseEntity<List<MaterialDto>> response = materialController.getAllMaterials(masterId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(materialService).getAllMaterialsByMasterId(masterId);
    }

    @Test
    void getAllMaterials_ShouldReturnEmptyList_WhenNoMaterials() {
        // given
        when(materialService.getAllMaterialsByMasterId(masterId)).thenReturn(Collections.emptyList());

        // when
        ResponseEntity<List<MaterialDto>> response = materialController.getAllMaterials(masterId);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(materialService).getAllMaterialsByMasterId(masterId);
    }
}