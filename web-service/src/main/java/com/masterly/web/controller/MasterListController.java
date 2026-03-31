package com.masterly.web.controller;

import com.masterly.web.client.CoreServiceClient;
import com.masterly.web.dto.MasterDto;
import com.masterly.web.dto.ServiceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/masters")
@RequiredArgsConstructor
public class MasterListController {

    private final CoreServiceClient coreServiceClient;

    @GetMapping
    public String listMasters(Model model) {
        log.info("Listing all masters");
        List<MasterDto> masters = coreServiceClient.getAllMasters();
        model.addAttribute("masters", masters);
        return "masters/list";
    }

    @GetMapping("/{id}")
    public String masterProfile(@PathVariable Long id, Model model) {
        log.info("Viewing master profile: {}", id);
        MasterDto master = coreServiceClient.getMasterById(id);
        model.addAttribute("master", master);

        // Получаем услуги мастера
        List<ServiceDto> services = coreServiceClient.getServicesByMasterId(id);
        model.addAttribute("services", services);

        return "masters/profile";
    }
}