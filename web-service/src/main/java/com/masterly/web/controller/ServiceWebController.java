package com.masterly.web.controller;

import com.masterly.web.client.CoreServiceClient;
import com.masterly.web.dto.ServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/services")
@RequiredArgsConstructor
public class ServiceWebController {

    private final CoreServiceClient coreServiceClient;

    @GetMapping
    public String listServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        log.debug("Listing services - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        Long masterId = 1L;

        Page<ServiceDto> servicePage = coreServiceClient.getServicesPaginated(
                page, size, sortBy, sortDir, masterId);

        log.debug("Found {} services total", servicePage.getTotalElements());

        model.addAttribute("services", servicePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", servicePage.getTotalPages());
        model.addAttribute("totalItems", servicePage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "services/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        log.debug("Showing create service form");
        model.addAttribute("service", new ServiceDto());
        return "services/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.debug("Showing edit form for service: {}", id);

        Long masterId = 1L;
        ServiceDto service = coreServiceClient.getService(id, masterId);

        log.debug("Loaded service: {}", service.getName());
        model.addAttribute("service", service);
        return "services/form";
    }

    @PostMapping("/save")
    public String saveService(@ModelAttribute ServiceDto serviceDto) {
        Long masterId = 1L;

        if (serviceDto.getId() == null) {
            log.info("Creating new service: {}", serviceDto.getName());
            coreServiceClient.createService(masterId, serviceDto);
            log.debug("Service created successfully");
        } else {
            log.info("Updating service: {}", serviceDto.getId());
            coreServiceClient.updateService(serviceDto.getId(), masterId, serviceDto);
            log.debug("Service {} updated successfully", serviceDto.getId());
        }

        return "redirect:/services";
    }

    @GetMapping("/delete/{id}")
    public String deleteService(@PathVariable Long id) {
        log.info("Deleting service: {}", id);

        coreServiceClient.deleteService(id);

        log.debug("Service {} deleted successfully", id);
        return "redirect:/services";
    }
}