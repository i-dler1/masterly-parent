package com.masterly.core.controller;

import com.masterly.core.dto.MasterCreateDto;
import com.masterly.core.dto.MasterDto;
import com.masterly.core.dto.MasterUpdateDto;
import com.masterly.core.service.MasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления мастерами.
 * Предоставляет API для регистрации, просмотра и редактирования профилей мастеров.
 */
@Slf4j
@RestController
@RequestMapping("/api/masters")
@RequiredArgsConstructor
public class MasterController {

    private final MasterService masterService;

    /**
     * Зарегистрировать нового мастера.
     *
     * @param createDto DTO с данными для регистрации (email, пароль, имя, телефон, специализация)
     * @return данные созданного мастера с присвоенным ID
     */
    @PostMapping
    public ResponseEntity<MasterDto> register(@RequestBody MasterCreateDto createDto) {
        log.info("Registering new master with email: {}", createDto.getEmail());

        MasterDto result = masterService.register(createDto);

        log.info("Master registered successfully with id: {}", result.getId());
        return ResponseEntity.ok(result);
    }

    /**
     * Получить профиль мастера по ID.
     *
     * @param id ID мастера
     * @return данные мастера
     */
    @GetMapping("/profile/{id}")
    public ResponseEntity<MasterDto> getProfile(@PathVariable Long id) {
        log.debug("Fetching master profile for id: {}", id);

        MasterDto master = masterService.getMasterById(id);

        log.debug("Master profile fetched: {}", master.getEmail());
        return ResponseEntity.ok(master);
    }

    /**
     * Обновить профиль мастера.
     *
     * @param id        ID мастера
     * @param updateDto DTO с обновлёнными данными
     * @return обновлённые данные мастера
     */
    @PutMapping("/profile/{id}")
    public ResponseEntity<MasterDto> updateProfile(@PathVariable Long id,
                                                   @RequestBody MasterUpdateDto updateDto) {
        log.info("Updating master profile for id: {}", id);

        MasterDto updated = masterService.updateMaster(id, updateDto);

        log.info("Master profile updated successfully for id: {}", id);
        return ResponseEntity.ok(updated);
    }

    /**
     * Получить список всех мастеров.
     *
     * @return список всех мастеров
     */
    @GetMapping
    public ResponseEntity<List<MasterDto>> getAllMasters() {
        log.info("GET /api/masters");
        List<MasterDto> masters = masterService.getAllMasters();
        return ResponseEntity.ok(masters);
    }

    /**
     * Получить мастера по ID.
     *
     * @param id ID мастера
     * @return данные мастера
     */
    @GetMapping("/{id}")
    public ResponseEntity<MasterDto> getMasterById(@PathVariable Long id) {
        log.info("GET /api/masters/{}", id);
        MasterDto master = masterService.getMasterById(id);
        return ResponseEntity.ok(master);
    }

    /**
     * Получить мастера по email.
     *
     * @param email email мастера
     * @return данные мастера
     */
    @GetMapping("/by-email")
    public ResponseEntity<MasterDto> getMasterByEmail(@RequestParam String email) {
        log.info("GET /api/masters/by-email - email: {}", email);
        MasterDto master = masterService.findByEmail(email);
        return ResponseEntity.ok(master);
    }
}