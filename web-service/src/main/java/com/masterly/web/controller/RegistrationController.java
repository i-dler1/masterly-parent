package com.masterly.web.controller;

import com.masterly.web.client.CoreAuthClient;
import com.masterly.web.dto.MasterRegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final CoreAuthClient coreAuthClient;

    @GetMapping
    public String showRegistrationForm() {
        log.debug("Showing registration form");
        return "register";
    }

    @PostMapping
    public String register(MasterRegisterRequest request) {
        log.info("New registration attempt for email: {}", request.getEmail());

        try {
            coreAuthClient.register(request);
            log.info("User registered successfully: {}", request.getEmail());
            return "redirect:/login?registered";
        } catch (Exception e) {
            log.warn("Registration failed for email: {} - {}", request.getEmail(), e.getMessage());
            return "redirect:/register?error";
        }
    }
}