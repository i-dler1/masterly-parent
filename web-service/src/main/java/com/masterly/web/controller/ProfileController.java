package com.masterly.web.controller;

import com.masterly.web.client.CoreServiceClient;
import com.masterly.web.dto.MasterDto;
import com.masterly.web.dto.MasterUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final CoreServiceClient coreServiceClient;

    @GetMapping
    public String showProfile(Authentication authentication, Model model) {
        String email = authentication.getName();
        log.debug("Showing profile for user: {}", email);

        // Получаем мастера по email (нужно добавить метод в CoreServiceClient)
        Long masterId = 1L; // временно, пока нет связи по email

        MasterDto master = coreServiceClient.getMasterProfile(masterId);
        log.debug("Loaded profile for master: {}", master.getEmail());

        model.addAttribute("master", master);

        return "profile/index";
    }

    @GetMapping("/edit")
    public String showEditForm(Authentication authentication, Model model) {
        String email = authentication.getName();
        log.debug("Showing edit form for user: {}", email);

        Long masterId = 1L;
        MasterDto master = coreServiceClient.getMasterProfile(masterId);

        MasterUpdateDto updateDto = new MasterUpdateDto();
        updateDto.setFullName(master.getFullName());
        updateDto.setPhone(master.getPhone());
        updateDto.setBusinessName(master.getBusinessName());
        updateDto.setSpecialization(master.getSpecialization());

        model.addAttribute("master", updateDto);

        return "profile/edit";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute MasterUpdateDto updateDto) {
        log.info("Updating master profile");
        log.debug("Updated details - name: {}, phone: {}, business: {}, specialization: {}",
                updateDto.getFullName(), updateDto.getPhone(),
                updateDto.getBusinessName(), updateDto.getSpecialization());

        Long masterId = 1L;
        coreServiceClient.updateMasterProfile(masterId, updateDto);

        log.info("Master profile updated successfully");
        return "redirect:/profile";
    }
}