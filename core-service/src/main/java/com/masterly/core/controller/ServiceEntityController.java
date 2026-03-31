package com.masterly.core.controller;

import com.masterly.core.dto.ServiceEntityDto;
import com.masterly.core.mapper.ServiceEntityMapper;
import com.masterly.core.model.Master;
import com.masterly.core.model.ServiceEntity;
import com.masterly.core.repository.MasterRepository;
import com.masterly.core.service.ServiceEntityService;
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
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceEntityController {

    private final ServiceEntityService serviceService;
    private final ServiceEntityMapper serviceMapper;
    private final MasterRepository masterRepository;

    @GetMapping
    public ResponseEntity<List<ServiceEntityDto>> getServices(@RequestParam Long masterId) {
        log.debug("Fetching all services for master: {}", masterId);

        List<ServiceEntity> services = serviceService.getAllServices(masterId);
        List<ServiceEntityDto> dtos = services.stream()
                .map(serviceMapper::toDto)
                .collect(Collectors.toList());

        log.debug("Found {} services", dtos.size());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceEntityDto> getService(@PathVariable Long id) {
        log.debug("Fetching service by id: {}", id);

        ServiceEntity service = serviceService.getService(id);
        if (service == null) {
            log.warn("Service not found with id: {}", id);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(serviceMapper.toDto(service));
    }

    @PostMapping
    public ResponseEntity<ServiceEntityDto> createService(@RequestParam Long masterId,
                                                          @RequestBody ServiceEntityDto serviceDto) {
        log.info("Creating new service for master: {}, name: {}", masterId, serviceDto.getName());
        log.debug("Service details: duration={}, price={}",
                serviceDto.getDurationMinutes(), serviceDto.getPrice());

        Master master = masterRepository.findById(masterId).orElse(null);
        if (master == null) {
            log.warn("Master not found for service creation - masterId: {}", masterId);
            return ResponseEntity.badRequest().build();
        }

        ServiceEntity service = serviceMapper.toEntity(serviceDto, master);
        ServiceEntity saved = serviceService.createService(service);

        log.info("Service created successfully with id: {}", saved.getId());
        return ResponseEntity.ok(serviceMapper.toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceEntityDto> updateService(@PathVariable Long id,
                                                          @RequestParam Long masterId,
                                                          @RequestBody ServiceEntityDto serviceDto) {
        log.info("Updating service: {} for master: {}", id, masterId);

        Master master = masterRepository.findById(masterId).orElse(null);
        if (master == null) {
            log.warn("Master not found for service update - masterId: {}", masterId);
            return ResponseEntity.badRequest().build();
        }

        ServiceEntity service = serviceMapper.toEntity(serviceDto, master);
        service.setId(id);
        ServiceEntity updated = serviceService.updateService(service);

        log.info("Service {} updated successfully", id);
        return ResponseEntity.ok(serviceMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        log.info("Deleting service: {}", id);

        serviceService.deleteService(id);

        log.info("Service {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<ServiceEntityDto>> getServicesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam Long masterId) {

        log.debug("Fetching services paginated - master: {}, page: {}, size: {}, sortBy: {}, sortDir: {}",
                masterId, page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ServiceEntityDto> services = serviceService.getServicesByMasterId(masterId, pageable);

        log.debug("Found {} services total", services.getTotalElements());
        return ResponseEntity.ok(services);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ServiceEntityDto>> getAllServices(@RequestParam Long masterId) {
        log.debug("Fetching all services DTOs for master: {}", masterId);

        List<ServiceEntityDto> dtos = serviceService.getAllServices(masterId).stream()
                .map(serviceMapper::toDto)
                .collect(Collectors.toList());

        log.debug("Found {} services", dtos.size());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/by-master/{masterId}")
    public ResponseEntity<List<ServiceEntityDto>> getServicesByMasterId(@PathVariable Long masterId) {
        log.info("GET /api/services/by-master/{}", masterId);
        List<ServiceEntityDto> services = serviceService.getServicesByMasterId(masterId);
        return ResponseEntity.ok(services);
    }
}