package com.masterly.core.controller;

import com.masterly.core.dto.MaterialDto;
import com.masterly.core.mapper.MaterialMapper;
import com.masterly.core.entity.Master;
import com.masterly.core.entity.Material;
import com.masterly.core.repository.MasterRepository;
import com.masterly.core.service.MaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер для управления материалами.
 * Предоставляет REST API для CRUD операций с материалами.
 */
@Slf4j
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final MaterialMapper materialMapper;
    private final MasterRepository masterRepository;

    /**
     * Получить все материалы мастера.
     *
     * @param masterId ID мастера
     * @return список материалов мастера
     */
    @GetMapping
    public ResponseEntity<List<MaterialDto>> getMaterials(@RequestParam Long masterId) {
        log.debug("Fetching all materials for master: {}", masterId);

        List<Material> materials = materialService.getAllMaterials(masterId);
        List<MaterialDto> dtos = materials.stream()
                .map(materialMapper::toDto)
                .collect(Collectors.toList());

        log.debug("Found {} materials", dtos.size());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Получить материал по ID.
     *
     * @param id ID материала
     * @return материал или 404 если не найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<MaterialDto> getMaterial(@PathVariable Long id) {
        log.debug("Fetching material by id: {}", id);

        Material material = materialService.getMaterial(id);
        if (material == null) {
            log.warn("Material not found with id: {}", id);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(materialMapper.toDto(material));
    }

    /**
     * Создать новый материал.
     *
     * @param masterId    ID мастера-владельца
     * @param materialDto DTO с данными материала
     * @return созданный материал
     */
    @PostMapping
    public ResponseEntity<MaterialDto> createMaterial(@RequestParam Long masterId,
                                                      @Valid @RequestBody MaterialDto materialDto) {
        log.info("Creating new material for master: {}, name: {}", masterId, materialDto.getName());
        log.debug("Material details: unit={}, quantity={}, price={}",
                materialDto.getUnit(), materialDto.getQuantity(), materialDto.getPricePerUnit());

        Master master = masterRepository.findById(masterId).orElse(null);
        if (master == null) {
            log.warn("Master not found for material creation - masterId: {}", masterId);
            return ResponseEntity.badRequest().build();
        }

        Material material = materialMapper.toEntity(materialDto);
        material.setMaster(master);
        Material saved = materialService.createMaterial(material);

        log.info("Material created successfully with id: {}", saved.getId());
        return ResponseEntity.ok(materialMapper.toDto(saved));
    }

    /**
     * Обновить существующий материал.
     *
     * @param id         ID материала
     * @param masterId   ID мастера-владельца
     * @param materialDto DTO с новыми данными
     * @return обновлённый материал
     */
    @PutMapping("/{id}")
    public ResponseEntity<MaterialDto> updateMaterial(@PathVariable Long id,
                                                      @RequestParam Long masterId,
                                                      @RequestBody MaterialDto materialDto) {
        log.info("Updating material: {} for master: {}", id, masterId);

        Master master = masterRepository.findById(masterId).orElse(null);
        if (master == null) {
            log.warn("Master not found for material update - masterId: {}", masterId);
            return ResponseEntity.badRequest().build();
        }

        Material material = materialMapper.toEntity(materialDto);
        material.setMaster(master);
        material.setId(id);
        Material updated = materialService.updateMaterial(material);

        log.info("Material {} updated successfully", id);
        return ResponseEntity.ok(materialMapper.toDto(updated));
    }

    /**
     * Удалить материал по ID.
     *
     * @param id ID материала
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        log.info("Deleting material: {}", id);

        materialService.deleteMaterial(id);

        log.info("Material {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получить материалы мастера с пагинацией и сортировкой.
     *
     * @param page    номер страницы (0-based)
     * @param size    размер страницы
     * @param sortBy  поле для сортировки
     * @param sortDir направление сортировки (asc/desc)
     * @param masterId ID мастера
     * @return страница с материалами
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<MaterialDto>> getMaterialsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam Long masterId) {

        log.debug("Fetching materials paginated - master: {}, page: {}, size: {}, sortBy: {}, sortDir: {}",
                masterId, page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<MaterialDto> materials = materialService.getMaterialsByMasterId(masterId, pageable);

        log.debug("Found {} materials total", materials.getTotalElements());
        return ResponseEntity.ok(materials);
    }

    /**
     * Получить все материалы для администратора с пагинацией.
     *
     * @param page    номер страницы (0-based)
     * @param size    размер страницы
     * @param sortBy  поле для сортировки
     * @param sortDir направление сортировки (asc/desc)
     * @return страница со всеми материалами
     */
    @GetMapping("/admin/all")
    public ResponseEntity<Page<MaterialDto>> getAllMaterialsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("GET /api/materials/admin/all - admin requesting all materials");
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(materialService.getMaterialsForAdmin(pageable));
    }

    /**
     * Получить все материалы мастера (альтернативный эндпоинт).
     *
     * @param masterId ID мастера
     * @return список всех материалов мастера
     */
    @GetMapping("/all")
    public ResponseEntity<List<MaterialDto>> getAllMaterials(@RequestParam Long masterId) {
        log.debug("Fetching all materials for master: {}", masterId);

        List<MaterialDto> materials = materialService.getAllMaterialsByMasterId(masterId);

        return ResponseEntity.ok(materials);
    }
}