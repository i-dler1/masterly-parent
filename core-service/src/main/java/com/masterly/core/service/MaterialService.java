package com.masterly.core.service;

import com.masterly.core.dto.MaterialDto;
import com.masterly.core.mapper.MaterialMapper;
import com.masterly.core.model.Material;
import com.masterly.core.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialMapper materialMapper;

    public List<Material> getAllMaterials(Long masterId) {
        log.debug("Fetching all materials for master: {}", masterId);
        return materialRepository.findByMasterId(masterId);
    }

    public Material getMaterial(Long id) {
        log.debug("Fetching material by id: {}", id);
        return materialRepository.findById(id).orElse(null);
    }

    public Material createMaterial(Material material) {
        log.info("Creating new material: {}", material.getName());
        log.debug("Material details - name: {}, unit: {}, quantity: {}, price: {}",
                material.getName(), material.getUnit(), material.getQuantity(), material.getPricePerUnit());

        Material saved = materialRepository.save(material);
        log.info("Material created successfully with id: {}", saved.getId());

        return saved;
    }

    public Material updateMaterial(Material material) {
        log.info("Updating material: {}", material.getId());
        log.debug("Updated material details - name: {}, quantity: {}, price: {}",
                material.getName(), material.getQuantity(), material.getPricePerUnit());

        Material updated = materialRepository.save(material);
        log.info("Material {} updated successfully", material.getId());

        return updated;
    }

    public void deleteMaterial(Long id) {
        log.info("Deleting material: {}", id);
        materialRepository.deleteById(id);
        log.debug("Material {} deleted", id);
    }

    public Page<MaterialDto> getMaterialsByMasterId(Long masterId, Pageable pageable) {
        log.debug("Fetching materials for master: {}, page: {}, size: {}",
                masterId, pageable.getPageNumber(), pageable.getPageSize());

        Page<Material> materials = materialRepository.findByMasterId(masterId, pageable);
        log.debug("Found {} materials", materials.getTotalElements());

        return materials.map(materialMapper::toDto);
    }
}