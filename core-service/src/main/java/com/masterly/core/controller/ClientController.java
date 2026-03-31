package com.masterly.core.controller;

import com.masterly.core.dto.ClientDto;
import com.masterly.core.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClient(@PathVariable Long id, @RequestParam Long masterId) {
        log.debug("Fetching client: {} for master: {}", id, masterId);

        ClientDto client = clientService.getClientByIdAndMasterId(id, masterId);
        if (client == null) {
            log.warn("Client not found - id: {}, master: {}", id, masterId);
            return ResponseEntity.notFound().build();
        }

        log.debug("Client found: {}", client.getFullName());
        return ResponseEntity.ok(client);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> updateClient(@PathVariable Long id,
                                                  @RequestParam Long masterId,
                                                  @Valid @RequestBody ClientDto clientDto) {
        log.info("Updating client: {} for master: {}", id, masterId);

        clientDto.setId(id);
        ClientDto updated = clientService.updateClient(clientDto, masterId);

        log.info("Client {} updated successfully", id);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<ClientDto>> getClientsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam Long masterId) {

        log.debug("Fetching clients paginated - master: {}, page: {}, size: {}",
                masterId, page, size);

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ClientDto> clients = clientService.getClientsByMasterId(masterId, pageable);

        log.debug("Found {} clients total", clients.getTotalElements());
        return ResponseEntity.ok(clients);
    }

    @PostMapping
    public ResponseEntity<ClientDto> createClient(@RequestParam Long masterId, @RequestBody ClientDto clientDto) {
        log.info("Creating new client for master: {}, name: {}", masterId, clientDto.getFullName());

        ClientDto created = clientService.createClient(clientDto, masterId);

        log.info("Client created successfully with id: {}", created.getId());
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id, @RequestParam Long masterId) {
        log.info("Deleting client: {} for master: {}", id, masterId);

        clientService.deleteClient(id, masterId);

        log.info("Client {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<ClientDto>> getAllClients(@RequestParam Long masterId) {
        log.debug("Fetching all clients for master: {}", masterId);

        List<ClientDto> clients = clientService.getAllClientsByMasterId(masterId);

        log.debug("Found {} clients", clients.size());
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/by-email")
    public ResponseEntity<ClientDto> getClientByEmail(@RequestParam String email) {
        log.info("GET /api/clients/by-email - email: {}", email);
        ClientDto client = clientService.findByEmail(email);
        return ResponseEntity.ok(client);
    }
}