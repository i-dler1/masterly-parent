package com.masterly.web.controller;

import com.masterly.web.client.CoreServiceClient;
import com.masterly.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/master")
@RequiredArgsConstructor
public class MasterWebController {

    private final CoreServiceClient coreServiceClient;

    // ==================== ПРОФИЛЬ ====================
    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        String email = authentication.getName();
        log.info("Master profile access for: {}", email);
        try {
            MasterDto master = coreServiceClient.getMasterById(1L); // временно
            model.addAttribute("master", master);
            return "masters/profile";
        } catch (Exception e) {
            log.error("Error loading master profile: {}", e.getMessage());
            return "error";
        }
    }

    // ==================== УСЛУГИ ====================
    @GetMapping("/services")
    public String services(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Authentication authentication,
                           Model model) {
        log.info("Master services page");
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            Page<ServiceDto> services = coreServiceClient.getServicesPaginated(page, size, "name", "asc", 1L);
            model.addAttribute("services", services);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", services.getTotalPages());
            return "masters/services";
        } catch (Exception e) {
            log.error("Error loading services: {}", e.getMessage());
            return "error";
        }
    }

    // ==================== МАТЕРИАЛЫ ====================
    @GetMapping("/materials")
    public String materials(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
        log.info("Master materials page");
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            Page<MaterialDto> materials = coreServiceClient.getMaterialsPaginated(page, size, "name", "asc", 1L);
            model.addAttribute("materials", materials);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", materials.getTotalPages());
            return "masters/materials";
        } catch (Exception e) {
            log.error("Error loading materials: {}", e.getMessage());
            return "error";
        }
    }

    // ==================== КЛИЕНТЫ ====================
    @GetMapping("/clients")
    public String clients(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {
        log.info("Master clients page");
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
            Page<ClientDto> clients = coreServiceClient.getClientsPaginated(page, size, "fullName", "asc", 1L);
            model.addAttribute("clients", clients);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", clients.getTotalPages());
            return "masters/clients";
        } catch (Exception e) {
            log.error("Error loading clients: {}", e.getMessage());
            return "error";
        }
    }

    // ==================== ЗАПИСИ ====================
    @GetMapping("/appointments")
    public String appointments(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model) {
        log.info("Master appointments page");
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentDate").descending());
            Page<AppointmentDto> appointments = coreServiceClient.getAppointmentsPaginated(page, size, "appointmentDate", "desc", 1L);
            model.addAttribute("appointments", appointments);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", appointments.getTotalPages());
            return "masters/appointments";
        } catch (Exception e) {
            log.error("Error loading appointments: {}", e.getMessage());
            return "error";
        }
    }

    // ==================== СЛОТЫ ВРЕМЕНИ ====================
    @GetMapping("/slots")
    public String slots(@RequestParam(required = false) Long serviceId,
                        @RequestParam(required = false) String date,
                        Model model) {
        log.info("Master slots page");
        return "masters/slots";
    }

    // ==================== УСЛУГИ - CRUD ====================

    @GetMapping("/services/new")
    public String showServiceForm(Model model) {
        model.addAttribute("service", new ServiceDto());
        model.addAttribute("isEdit", false);
        return "masters/service-form";
    }

    @GetMapping("/services/edit/{id}")
    public String editService(@PathVariable Long id, Model model) {
        try {
            ServiceDto service = coreServiceClient.getService(id, 1L);
            model.addAttribute("service", service);
            model.addAttribute("isEdit", true);
            return "masters/service-form";
        } catch (Exception e) {
            log.error("Error loading service for edit: {}", e.getMessage());
            return "redirect:/master/services?error=notfound";
        }
    }

    @PostMapping("/services/save")
    public String saveService(@ModelAttribute ServiceDto serviceDto, Model model) {
        try {
            if (serviceDto.getId() == null) {
                coreServiceClient.createService(1L, serviceDto);
                log.info("Service created: {}", serviceDto.getName());
            } else {
                coreServiceClient.updateService(serviceDto.getId(), 1L, serviceDto);
                log.info("Service updated: {}", serviceDto.getName());
            }
            return "redirect:/master/services?success";
        } catch (Exception e) {
            log.error("Error saving service: {}", e.getMessage());
            model.addAttribute("error", "Ошибка сохранения услуги");
            return "masters/service-form";
        }
    }

    @GetMapping("/services/delete/{id}")
    public String deleteService(@PathVariable Long id) {
        try {
            coreServiceClient.deleteService(id);
            log.info("Service deleted: {}", id);
            return "redirect:/master/services?deleted";
        } catch (Exception e) {
            log.error("Error deleting service: {}", e.getMessage());
            return "redirect:/master/services?error";
        }
    }

    // ==================== МАТЕРИАЛЫ - CRUD ====================

    @GetMapping("/materials/new")
    public String showMaterialForm(Model model) {
        model.addAttribute("material", new MaterialDto());
        model.addAttribute("isEdit", false);
        return "masters/material-form";
    }

    @GetMapping("/materials/edit/{id}")
    public String editMaterial(@PathVariable Long id, Model model) {
        try {
            MaterialDto material = coreServiceClient.getMaterial(id, 1L);
            model.addAttribute("material", material);
            model.addAttribute("isEdit", true);
            return "masters/material-form";
        } catch (Exception e) {
            log.error("Error loading material for edit: {}", e.getMessage());
            return "redirect:/master/materials?error=notfound";
        }
    }

    @PostMapping("/materials/save")
    public String saveMaterial(@ModelAttribute MaterialDto materialDto, Model model) {
        try {
            if (materialDto.getId() == null) {
                coreServiceClient.createMaterial(1L, materialDto);
                log.info("Material created: {}", materialDto.getName());
            } else {
                coreServiceClient.updateMaterial(materialDto.getId(), 1L, materialDto);
                log.info("Material updated: {}", materialDto.getName());
            }
            return "redirect:/master/materials?success";
        } catch (Exception e) {
            log.error("Error saving material: {}", e.getMessage());
            model.addAttribute("error", "Ошибка сохранения материала");
            return "masters/material-form";
        }
    }

    @GetMapping("/materials/delete/{id}")
    public String deleteMaterial(@PathVariable Long id) {
        try {
            coreServiceClient.deleteMaterial(id);
            log.info("Material deleted: {}", id);
            return "redirect:/master/materials?deleted";
        } catch (Exception e) {
            log.error("Error deleting material: {}", e.getMessage());
            return "redirect:/master/materials?error";
        }
    }

    // ==================== КЛИЕНТЫ - CRUD ====================

    @GetMapping("/clients/new")
    public String showClientForm(Model model) {
        model.addAttribute("client", new ClientDto());
        model.addAttribute("isEdit", false);
        return "masters/client-form";
    }

    @GetMapping("/clients/edit/{id}")
    public String editClient(@PathVariable Long id, Model model) {
        try {
            ClientDto client = coreServiceClient.getClient(id, 1L);
            model.addAttribute("client", client);
            model.addAttribute("isEdit", true);
            return "masters/client-form";
        } catch (Exception e) {
            log.error("Error loading client for edit: {}", e.getMessage());
            return "redirect:/master/clients?error=notfound";
        }
    }

    @PostMapping("/clients/save")
    public String saveClient(@ModelAttribute ClientDto clientDto, Model model) {
        try {
            if (clientDto.getId() == null) {
                coreServiceClient.createClient(1L, clientDto);
                log.info("Client created: {}", clientDto.getFullName());
            } else {
                coreServiceClient.updateClient(clientDto.getId(), 1L, clientDto);
                log.info("Client updated: {}", clientDto.getFullName());
            }
            return "redirect:/master/clients?success";
        } catch (Exception e) {
            log.error("Error saving client: {}", e.getMessage());
            model.addAttribute("error", "Ошибка сохранения клиента");
            return "masters/client-form";
        }
    }

    @GetMapping("/clients/delete/{id}")
    public String deleteClient(@PathVariable Long id) {
        try {
            coreServiceClient.deleteClient(id, 1L);
            log.info("Client deleted: {}", id);
            return "redirect:/master/clients?deleted";
        } catch (Exception e) {
            log.error("Error deleting client: {}", e.getMessage());
            return "redirect:/master/clients?error";
        }
    }

    // ==================== ЗАПИСИ - ИЗМЕНЕНИЕ СТАТУСА ====================

    @PostMapping("/appointments/{id}/confirm")
    public String confirmAppointment(@PathVariable Long id) {
        try {
            coreServiceClient.updateAppointmentStatus(id, "CONFIRMED");
            log.info("Appointment {} confirmed", id);
            return "redirect:/master/appointments?confirmed";
        } catch (Exception e) {
            log.error("Error confirming appointment: {}", e.getMessage());
            return "redirect:/master/appointments?error";
        }
    }

    @PostMapping("/appointments/{id}/complete")
    public String completeAppointment(@PathVariable Long id) {
        try {
            coreServiceClient.updateAppointmentStatus(id, "COMPLETED");
            log.info("Appointment {} completed", id);
            return "redirect:/master/appointments?completed";
        } catch (Exception e) {
            log.error("Error completing appointment: {}", e.getMessage());
            return "redirect:/master/appointments?error";
        }
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id) {
        try {
            coreServiceClient.updateAppointmentStatus(id, "CANCELLED");
            log.info("Appointment {} cancelled", id);
            return "redirect:/master/appointments?cancelled";
        } catch (Exception e) {
            log.error("Error cancelling appointment: {}", e.getMessage());
            return "redirect:/master/appointments?error";
        }
    }
}