package com.masterly.web.controller;

import com.masterly.web.client.CoreServiceClient;
import com.masterly.web.dto.MaterialDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/materials")
@RequiredArgsConstructor
public class MaterialWebController {

    private final CoreServiceClient coreServiceClient;

    @GetMapping
    public String listMaterials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        log.debug("Listing materials - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        Long masterId = 1L;

        Page<MaterialDto> materialPage = coreServiceClient.getMaterialsPaginated(
                page, size, sortBy, sortDir, masterId);

        log.debug("Found {} materials total", materialPage.getTotalElements());

        model.addAttribute("materials", materialPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", materialPage.getTotalPages());
        model.addAttribute("totalItems", materialPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "materials/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        log.debug("Showing create material form");
        model.addAttribute("material", new MaterialDto());
        return "materials/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.debug("Showing edit form for material: {}", id);

        Long masterId = 1L;
        MaterialDto material = coreServiceClient.getMaterial(id, masterId);

        log.debug("Loaded material: {}", material.getName());
        model.addAttribute("material", material);
        return "materials/form";
    }

    @PostMapping("/save")
    public String saveMaterial(@ModelAttribute MaterialDto materialDto) {
        Long masterId = 1L;

        if (materialDto.getId() == null) {
            log.info("Creating new material: {}", materialDto.getName());
            coreServiceClient.createMaterial(masterId, materialDto);
            log.debug("Material created successfully");
        } else {
            log.info("Updating material: {}", materialDto.getId());
            coreServiceClient.updateMaterial(materialDto.getId(), masterId, materialDto);
            log.debug("Material {} updated successfully", materialDto.getId());
        }

        return "redirect:/materials";
    }

    @GetMapping("/delete/{id}")
    public String deleteMaterial(@PathVariable Long id) {
        log.info("Deleting material: {}", id);

        coreServiceClient.deleteMaterial(id);

        log.debug("Material {} deleted successfully", id);
        return "redirect:/materials";
    }
}