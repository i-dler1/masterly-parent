package com.masterly.web.controller;

import com.masterly.web.client.CoreServiceClient;
import com.masterly.web.dto.AppointmentDto;
import com.masterly.web.dto.ClientDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/client")
@RequiredArgsConstructor
public class ClientProfileController {

    private final CoreServiceClient coreServiceClient;

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        String email = authentication.getName();
        log.info("Client profile access for: {}", email);

        try {
            ClientDto client = coreServiceClient.getClientByEmail(email);
            model.addAttribute("client", client);

            List<AppointmentDto> appointments = coreServiceClient.getAppointmentsByClientId(client.getId());
            model.addAttribute("appointments", appointments);

            return "clients/profile";

        } catch (Exception e) {
            log.error("Error loading client profile: {}", e.getMessage());
            model.addAttribute("error", "Ошибка загрузки профиля: " + e.getMessage());
            return "error";
        }
    }
}