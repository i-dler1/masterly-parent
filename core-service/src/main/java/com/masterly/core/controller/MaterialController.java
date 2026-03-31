package com.masterly.core.controller;

import com.masterly.core.dto.MaterialDto;
import com.masterly.core.mapper.MaterialMapper;
import com.masterly.core.model.Master;
import com.masterly.core.model.Material;
import com.masterly.core.repository.MasterRepository;
import com.masterly.core.service.MaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final MaterialMapper materialMapper;
    private final MasterRepository masterRepository;

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

    @PostMapping
    public ResponseEntity<MaterialDto> createMaterial(@RequestParam Long masterId,
                                                      @RequestBody MaterialDto materialDto) {
        log.info("Creating new material for master: {}, name: {}", masterId, materialDto.getName());
        log.debug("Material details: unit={}, quantity={}, price={}",
                materialDto.getUnit(), materialDto.getQuantity(), materialDto.getPricePerUnit());

        Master master = masterRepository.findById(masterId).orElse(null);
        if (master == null) {
            log.warn("Master not found for material creation - masterId: {}", masterId);
            return ResponseEntity.badRequest().build();
        }

        Material material = materialMapper.toEntity(materialDto, master);
        Material saved = materialService.createMaterial(material);

        log.info("Material created successfully with id: {}", saved.getId());
        return ResponseEntity.ok(materialMapper.toDto(saved));
    }

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

        Material material = materialMapper.toEntity(materialDto, master);
        material.setId(id);
        Material updated = materialService.updateMaterial(material);

        log.info("Material {} updated successfully", id);
        return ResponseEntity.ok(materialMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        log.info("Deleting material: {}", id);

        materialService.deleteMaterial(id);

        log.info("Material {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

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
}