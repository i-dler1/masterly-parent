package com.masterly.web.controller;

import com.masterly.web.client.CoreServiceClient;
import com.masterly.web.dto.AppointmentCreateDto;
import com.masterly.web.dto.AppointmentDto;
import com.masterly.web.dto.ClientDto;
import com.masterly.web.dto.ServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentWebController {

    private final CoreServiceClient coreServiceClient;

    @GetMapping
    public String listAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        log.debug("Listing appointments - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        Long masterId = 1L;

        Page<AppointmentDto> appointmentPage = coreServiceClient.getAppointmentsPaginated(
                page, size, sortBy, sortDir, masterId);

        log.debug("Found {} appointments total", appointmentPage.getTotalElements());

        model.addAttribute("appointments", appointmentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", appointmentPage.getTotalPages());
        model.addAttribute("totalItems", appointmentPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

        return "appointments/list";
    }

//    @GetMapping("/new")
//    public String showCreateForm(Model model) {
//        log.debug("Showing create appointment form");
//
//        Long masterId = 1L;
//
//        List<ClientDto> clients = coreServiceClient.getAllClients(masterId);
//        List<ServiceDto> services = coreServiceClient.getAllServices(masterId);
//
//        log.debug("Loaded {} clients and {} services for form", clients.size(), services.size());
//
//        model.addAttribute("appointment", new AppointmentCreateDto());
//        model.addAttribute("clients", clients);
//        model.addAttribute("services", services);
//        model.addAttribute("today", LocalDate.now());
//
//        return "appointments/form";
//    }

    @PostMapping("/save")
    public String saveAppointment(@ModelAttribute AppointmentCreateDto createDto,
                                  @RequestParam(required = false) Long id) {

        if (id == null) {
            log.info("Creating new appointment for client: {}, service: {}, date: {}, time: {}",
                    createDto.getClientId(), createDto.getServiceId(),
                    createDto.getAppointmentDate(), createDto.getStartTime());
        } else {
            log.info("Updating appointment: {}", id);
        }

        createDto.setMasterId(1L);

        if (id == null) {
            coreServiceClient.createAppointment(createDto);
            log.debug("Appointment created successfully");
        } else {
            coreServiceClient.updateAppointment(id, createDto);
            log.debug("Appointment {} updated successfully", id);
        }

        return "redirect:/appointments";
    }

    @GetMapping("/delete/{id}")
    public String deleteAppointment(@PathVariable Long id) {
        log.info("Deleting appointment: {}", id);

        coreServiceClient.deleteAppointment(id);

        log.debug("Appointment {} deleted", id);
        return "redirect:/appointments";
    }

    @GetMapping("/status/{id}")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("Updating appointment {} status to: {}", id, status);

        coreServiceClient.updateAppointmentStatus(id, status);

        log.debug("Appointment {} status updated to: {}", id, status);
        return "redirect:/appointments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.debug("Showing edit form for appointment: {}", id);

        Long masterId = 1L;

        // Получаем запись для редактирования
        AppointmentDto appointment = coreServiceClient.getAppointment(id);
        log.debug("Loaded appointment: client={}, service={}, date={}, time={}",
                appointment.getClientId(), appointment.getServiceId(),
                appointment.getAppointmentDate(), appointment.getStartTime());

        // Загружаем клиентов и услуги для выпадающих списков
        List<ClientDto> clients = coreServiceClient.getAllClients(masterId);
        List<ServiceDto> services = coreServiceClient.getAllServices(masterId);
        log.debug("Loaded {} clients and {} services for edit form", clients.size(), services.size());

        // Создаем DTO для формы из существующей записи
        AppointmentCreateDto createDto = new AppointmentCreateDto();
        createDto.setClientId(appointment.getClientId());
        createDto.setServiceId(appointment.getServiceId());
        createDto.setAppointmentDate(appointment.getAppointmentDate());
        createDto.setStartTime(appointment.getStartTime());
        createDto.setNotes(appointment.getNotes());

        model.addAttribute("appointment", createDto);
        model.addAttribute("appointmentId", id);
        model.addAttribute("clients", clients);
        model.addAttribute("services", services);
        model.addAttribute("today", LocalDate.now());

        return "appointments/form";
    }
}