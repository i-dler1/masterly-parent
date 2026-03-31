package com.masterly.web.controller;

import com.masterly.web.client.CoreServiceClient;
import com.masterly.web.dto.ClientDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientWebController {

    private final CoreServiceClient coreServiceClient;

    @GetMapping
    public String listClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        log.debug("Listing clients - page: {}, size: {}, sortBy: {}, sortDir: {}",
                page, size, sortBy, sortDir);

        Long masterId = 1L;

        Page<ClientDto> clientPage = coreServiceClient.getClientsPaginated(
                page, size, sortBy, sortDir, masterId);

        log.debug("Found {} clients total", clientPage.getTotalElements());

        model.addAttribute("clients", clientPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", clientPage.getTotalPages());
        model.addAttribute("totalItems", clientPage.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("startIndex", page * size + 1);

        return "clients/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        log.debug("Showing create client form");
        model.addAttribute("client", new ClientDto());
        return "clients/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.debug("Showing edit form for client: {}", id);

        Long masterId = 1L;
        ClientDto client = coreServiceClient.getClient(id, masterId);

        log.debug("Loaded client: {}", client.getFullName());
        model.addAttribute("client", client);
        return "clients/form";
    }

    @PostMapping("/save")
    public String saveClient(@Valid @ModelAttribute("client") ClientDto clientDto,
                             BindingResult result,
                             Model model) {

        if (result.hasErrors()) {
            log.warn("Validation errors while saving client: {}", result.getAllErrors());
            return "clients/form";
        }

        Long masterId = 1L;

        if (clientDto.getId() == null) {
            log.info("Creating new client: {}", clientDto.getFullName());
            coreServiceClient.createClient(masterId, clientDto);
            log.debug("Client created successfully");
        } else {
            log.info("Updating client: {}", clientDto.getId());
            coreServiceClient.updateClient(clientDto.getId(), masterId, clientDto);
            log.debug("Client {} updated successfully", clientDto.getId());
        }

        return "redirect:/clients";
    }

    @GetMapping("/delete/{id}")
    public String deleteClient(@PathVariable Long id) {
        log.info("Deleting client: {}", id);

        Long masterId = 1L;
        coreServiceClient.deleteClient(id, masterId);

        log.debug("Client {} deleted successfully", id);
        return "redirect:/clients";
    }
}