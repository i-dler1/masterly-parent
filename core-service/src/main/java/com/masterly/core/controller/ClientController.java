package com.masterly.core.controller;

import com.masterly.core.dto.ClientDto;
import com.masterly.core.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления клиентами.
 * Предоставляет REST API для CRUD операций с клиентами.
 */
@Slf4j
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    /**
     * Получить клиента по ID с проверкой принадлежности мастеру.
     *
     * @param id       ID клиента
     * @param masterId ID мастера-владельца
     * @return клиент или 404 если не найден
     */
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

    /**
     * Обновить данные клиента.
     *
     * @param id        ID клиента
     * @param masterId  ID мастера-владельца
     * @param clientDto DTO с новыми данными
     * @return обновлённый клиент
     */
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

    /**
     * Получить клиентов мастера с пагинацией и сортировкой.
     *
     * @param page    номер страницы (0-based)
     * @param size    размер страницы
     * @param sortBy  поле для сортировки
     * @param sortDir направление сортировки (asc/desc)
     * @param masterId ID мастера
     * @return страница с клиентами
     */
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

    /**
     * Создать нового клиента.
     *
     * @param masterId  ID мастера-владельца
     * @param clientDto DTO с данными клиента
     * @return созданный клиент
     */
    @PostMapping
    public ResponseEntity<ClientDto> createClient(
            @RequestParam Long masterId,
            @Valid @RequestBody ClientDto clientDto) {
        log.info("Creating new client for master: {}, name: {}", masterId, clientDto.getFullName());

        ClientDto created = clientService.createClient(clientDto, masterId);

        log.info("Client created successfully with id: {}", created.getId());
        return ResponseEntity.ok(created);
    }

    /**
     * Удалить клиента. Если есть связанные записи — вернёт 409 Conflict.
     *
     * @param id       ID клиента
     * @param masterId ID мастера-владельца
     * @return 204 No Content при успехе, 409 если есть записи
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id, @RequestParam Long masterId) {
        log.info("Deleting client: {} for master: {}", id, masterId);

        try {
            clientService.deleteClient(id, masterId);
            log.info("Client {} deleted successfully", id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("записи")) {
                log.warn("Cannot delete client {} - has appointments", id);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
            }
            throw e;
        }
    }

    /**
     * Получить всех клиентов мастера.
     *
     * @param masterId ID мастера
     * @return список всех клиентов мастера
     */
    @GetMapping("/all")
    public ResponseEntity<List<ClientDto>> getAllClients(@RequestParam Long masterId) {
        log.debug("Fetching all clients for master: {}", masterId);

        List<ClientDto> clients = clientService.getAllClientsByMasterId(masterId);

        log.debug("Found {} clients", clients.size());
        return ResponseEntity.ok(clients);
    }

    /**
     * Получить клиента по email.
     *
     * @param email email клиента
     * @return данные клиента
     */
    @GetMapping("/by-email")
    public ResponseEntity<ClientDto> getClientByEmail(@RequestParam String email) {
        log.info("GET /api/clients/by-email - email: {}", email);
        ClientDto client = clientService.findByEmail(email);
        return ResponseEntity.ok(client);
    }

    /**
     * Получить всех клиентов для администратора с пагинацией.
     *
     * @param page    номер страницы (0-based)
     * @param size    размер страницы
     * @param sortBy  поле для сортировки
     * @param sortDir направление сортировки (asc/desc)
     * @return страница со всеми клиентами
     */
    @GetMapping("/admin/all")
    public ResponseEntity<Page<ClientDto>> getAllClientsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.info("GET /api/clients/admin/all - admin requesting all clients");
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(clientService.getClientsForAdmin(pageable));
    }

    /**
     * Обновить профиль клиента (без привязки к мастеру).
     *
     * @param id        ID клиента
     * @param clientDto DTO с новыми данными
     * @return обновлённый клиент
     */
    @PutMapping("/{id}/profile")
    public ResponseEntity<ClientDto> updateClientProfile(@PathVariable Long id,
                                                         @Valid @RequestBody ClientDto clientDto) {
        log.info("Updating client profile: {}", id);

        ClientDto updated = clientService.updateClientProfile(id, clientDto);

        return ResponseEntity.ok(updated);
    }
}