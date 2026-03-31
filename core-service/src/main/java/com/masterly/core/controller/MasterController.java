package com.masterly.core.controller;

import com.masterly.core.dto.MasterCreateDto;
import com.masterly.core.dto.MasterDto;
import com.masterly.core.dto.MasterUpdateDto;
import com.masterly.core.service.MasterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/masters")
@RequiredArgsConstructor
public class MasterController {

    private final MasterService masterService;

    @PostMapping
    public ResponseEntity<MasterDto> register(@RequestBody MasterCreateDto createDto) {
        log.info("Registering new master with email: {}", createDto.getEmail());

        MasterDto result = masterService.register(createDto);

        log.info("Master registered successfully with id: {}", result.getId());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<MasterDto> getProfile(@PathVariable Long id) {
        log.debug("Fetching master profile for id: {}", id);

        MasterDto master = masterService.getMasterById(id);

        log.debug("Master profile fetched: {}", master.getEmail());
        return ResponseEntity.ok(master);
    }

    @PutMapping("/profile/{id}")
    public ResponseEntity<MasterDto> updateProfile(@PathVariable Long id,
                                                   @RequestBody MasterUpdateDto updateDto) {
        log.info("Updating master profile for id: {}", id);

        MasterDto updated = masterService.updateMaster(id, updateDto);

        log.info("Master profile updated successfully for id: {}", id);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<MasterDto>> getAllMasters() {
        log.info("GET /api/masters");
        List<MasterDto> masters = masterService.getAllMasters();
        return ResponseEntity.ok(masters);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MasterDto> getMasterById(@PathVariable Long id) {
        log.info("GET /api/masters/{}", id);
        MasterDto master = masterService.getMasterById(id);
        return ResponseEntity.ok(master);
    }
}