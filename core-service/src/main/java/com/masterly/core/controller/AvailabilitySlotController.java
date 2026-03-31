package com.masterly.core.controller;

import com.masterly.core.dto.AvailabilitySlotDto;
import com.masterly.core.service.AvailabilitySlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilitySlotController {

    private final AvailabilitySlotService slotService;

    @GetMapping("/slots")
    public ResponseEntity<List<AvailabilitySlotDto>> getFreeSlots(
            @RequestParam Long masterId,
            @RequestParam(required = false) Long serviceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("GET /api/availability/slots - master: {}, service: {}, date: {}", masterId, serviceId, date);
        List<AvailabilitySlotDto> slots = slotService.getFreeSlots(masterId, serviceId, date);
        return ResponseEntity.ok(slots);
    }

    @PostMapping("/slots")
    public ResponseEntity<AvailabilitySlotDto> createSlot(@RequestBody AvailabilitySlotDto slotDto) {
        log.info("POST /api/availability/slots - master: {}, service: {}, date: {}",
                slotDto.getMasterId(), slotDto.getServiceId(), slotDto.getSlotDate());
        AvailabilitySlotDto created = slotService.createSlot(slotDto);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/slots/{slotId}/book")
    public ResponseEntity<Void> bookSlot(@PathVariable Long slotId) {
        log.info("POST /api/availability/slots/{}/book", slotId);
        slotService.bookSlot(slotId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/slots/{slotId}/release")
    public ResponseEntity<Void> releaseSlot(@PathVariable Long slotId) {
        log.info("POST /api/availability/slots/{}/release", slotId);
        slotService.releaseSlot(slotId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long slotId) {
        log.info("DELETE /api/availability/slots/{}", slotId);
        slotService.deleteSlot(slotId);
        return ResponseEntity.noContent().build();
    }
}