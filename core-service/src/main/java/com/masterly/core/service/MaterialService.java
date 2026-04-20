package com.masterly.core.service;

import com.masterly.core.dto.MaterialDto;
import com.masterly.core.mapper.MaterialMapper;
import com.masterly.core.entity.Material;
import com.masterly.core.entity.ServiceMaterial;
import com.masterly.core.repository.MaterialRepository;
import com.masterly.core.repository.ServiceMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления материалами.
 * Предоставляет бизнес-логику для создания, обновления, удаления и поиска материалов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialMapper materialMapper;
    private final ServiceMaterialRepository serviceMaterialRepository;

    /**
     * Получить все материалы мастера.
     *
     * @param masterId ID мастера
     * @return список материалов мастера
     */
    public List<Material> getAllMaterials(Long masterId) {
        log.debug("Fetching all materials for master: {}", masterId);
        return materialRepository.findByMasterId(masterId);
    }

    /**
     * Получить материал по ID.
     *
     * @param id ID материала
     * @return материал или null если не найден
     */
    public Material getMaterial(Long id) {
        log.debug("Fetching material by id: {}", id);
        return materialRepository.findById(id).orElse(null);
    }

    /**
     * Создать новый материал.
     *
     * @param material материал для создания
     * @return созданный материал
     */
    public Material createMaterial(Material material) {
        log.info("Creating new material: {}", material.getName());
        log.debug("Material details - name: {}, unit: {}, quantity: {}, price: {}",
                material.getName(), material.getUnit(), material.getQuantity(), material.getPricePerUnit());

        Material saved = materialRepository.save(material);
        log.info("Material created successfully with id: {}", saved.getId());

        return saved;
    }

    /**
     * Обновить существующий материал.
     *
     * @param material материал с обновлёнными данными
     * @return обновлённый материал
     */
    public Material updateMaterial(Material material) {
        log.info("Updating material: {}", material.getId());
        log.debug("Updated material details - name: {}, quantity: {}, price: {}",
                material.getName(), material.getQuantity(), material.getPricePerUnit());

        Material updated = materialRepository.save(material);
        log.info("Material {} updated successfully", material.getId());

        return updated;
    }

    /**
     * Удалить материал по ID.
     *
     * @param id ID материала
     * @throws RuntimeException если материал используется в услугах
     */
    public void deleteMaterial(Long id) {
        log.info("Deleting material: {}", id);

        // Проверяем, используется ли материал в услугах
        List<ServiceMaterial> serviceMaterials = serviceMaterialRepository.findByMaterialId(id);
        if (!serviceMaterials.isEmpty()) {
            log.warn("Cannot delete material {} - used in services", id);
            throw new RuntimeException("Нельзя удалить материал, так как он используется в услугах");
        }

        materialRepository.deleteById(id);
        log.debug("Material {} deleted", id);
    }

    /**
     * Получить материалы мастера с пагинацией.
     *
     * @param masterId ID мастера
     * @param pageable параметры пагинации
     * @return страница с материалами
     */
    public Page<MaterialDto> getMaterialsByMasterId(Long masterId, Pageable pageable) {
        log.debug("Fetching materials for master: {}, page: {}, size: {}",
                masterId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Material> materials = materialRepository.findByMasterId(masterId, pageable);
        log.debug("Found {} materials", materials.getTotalElements());

        return materials.map(materialMapper::toDto);
    }

    /**
     * Получить все материалы для администратора с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница со всеми материалами
     */
    public Page<MaterialDto> getMaterialsForAdmin(Pageable pageable) {
        log.debug("Fetching all materials for admin");
        Page<Material> materials = materialRepository.findAll(pageable);
        return materials.map(materialMapper::toDto);
    }

    /**
     * Получить все материалы мастера в виде DTO.
     *
     * @param masterId ID мастера
     * @return список DTO материалов мастера
     */
    public List<MaterialDto> getAllMaterialsByMasterId(Long masterId) {
        log.debug("Fetching all materials for master: {}", masterId);

        List<Material> materials = materialRepository.findByMasterId(masterId);

        return materials.stream()
                .map(materialMapper::toDto)
                .collect(Collectors.toList());
    }
}