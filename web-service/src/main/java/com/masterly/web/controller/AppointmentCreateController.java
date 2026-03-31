package com.masterly.web.controller;

import com.masterly.web.client.CoreServiceClient;
import com.masterly.web.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentCreateController {

    private final CoreServiceClient coreServiceClient;

    @GetMapping("/new")
    public String showCreateForm(@RequestParam Long masterId,
                                 @RequestParam(required = false) Long serviceId,
                                 Authentication authentication,
                                 Model model) {
        String email = authentication.getName();
        log.info("Showing appointment form for master: {}, service: {}, user: {}", masterId, serviceId, email);

        try {
            ClientDto client = coreServiceClient.getClientByEmail(email);
            model.addAttribute("client", client);

            MasterDto master = coreServiceClient.getMasterById(masterId);
            model.addAttribute("master", master);

            List<ServiceDto> services = coreServiceClient.getServicesByMasterId(masterId);
            model.addAttribute("services", services);

            AppointmentCreateDto appointmentDto = new AppointmentCreateDto();
            appointmentDto.setMasterId(masterId);
            appointmentDto.setClientId(client.getId());

            // Если передан serviceId, предустанавливаем его
            if (serviceId != null) {
                appointmentDto.setServiceId(serviceId);
                log.debug("Preselecting service: {}", serviceId);
            }

            model.addAttribute("appointment", appointmentDto);

            // Получаем доступные слоты для выбранной даты (по умолчанию завтра)
            LocalDate defaultDate = LocalDate.now().plusDays(1);
            List<AvailabilitySlotDto> slots = coreServiceClient.getFreeSlots(masterId, serviceId, defaultDate.toString());
            model.addAttribute("slots", slots);
            model.addAttribute("selectedDate", defaultDate);

            return "appointments/create-form";

        } catch (Exception e) {
            log.error("Error loading appointment form: {}", e.getMessage());
            model.addAttribute("error", "Ошибка загрузки формы записи");
            return "error";
        }
    }

    @PostMapping("/create")
    public String createAppointment(@ModelAttribute AppointmentCreateDto appointmentDto, Model model) {
        log.info("Creating appointment for client: {}, master: {}, service: {}, date: {}",
                appointmentDto.getClientId(), appointmentDto.getMasterId(),
                appointmentDto.getServiceId(), appointmentDto.getAppointmentDate());

        try {
            coreServiceClient.createAppointment(appointmentDto);
            log.info("Appointment created successfully");
            return "redirect:/my-appointments";

        } catch (feign.FeignException e) {
            if (e.status() == 409) {
                log.warn("Time slot already occupied");
                model.addAttribute("error", "Это время уже занято. Пожалуйста, выберите другое время.");
            } else {
                log.error("Error creating appointment: {}", e.getMessage());
                model.addAttribute("error", "Ошибка создания записи: " + e.getMessage());
            }

            // Возвращаем пользователя обратно на форму с сохранёнными данными
            try {
                ClientDto client = coreServiceClient.getClientByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
                MasterDto master = coreServiceClient.getMasterById(appointmentDto.getMasterId());
                List<ServiceDto> services = coreServiceClient.getServicesByMasterId(appointmentDto.getMasterId());

                model.addAttribute("client", client);
                model.addAttribute("master", master);
                model.addAttribute("services", services);
                model.addAttribute("appointment", appointmentDto);

                return "appointments/create-form";

            } catch (Exception ex) {
                log.error("Error reloading form: {}", ex.getMessage());
                return "error";
            }

        } catch (Exception e) {
            log.error("Error creating appointment: {}", e.getMessage());
            model.addAttribute("error", "Ошибка создания записи: " + e.getMessage());
            return "error";
        }
    }
}