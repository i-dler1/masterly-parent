package com.masterly.core.controller;

import com.masterly.core.dto.ServiceEntityDto;
import com.masterly.core.dto.ServiceMaterialDto;
import com.masterly.core.mapper.ServiceEntityMapper;
import com.masterly.core.entity.Master;
import com.masterly.core.entity.ServiceEntity;
import com.masterly.core.repository.MasterRepository;
import com.masterly.core.service.ServiceEntityService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер для управления услугами.
 * Предоставляет REST API для CRUD операций с услугами, а также управления материалами услуги.
 */
@Slf4j
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceEntityController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT_BY = "id";
    private static final String DEFAULT_SORT_DIR = "asc";
    private static final String SORT_ASC = "asc";

    private final ServiceEntityService serviceEntityService;
    private final ServiceEntityMapper serviceEntityMapper;
    private final MasterRepository masterRepository;

    /**
     * Получить услугу по ID.
     *
     * @param id ID услуги
     * @return услуга или 404 если не найдена
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceEntityDto> getService(@PathVariable Long id) {
        log.debug("Fetching service by id: {}", id);

        ServiceEntity service = serviceEntityService.getService(id);
        if (service == null) {
            log.warn("Service not found with id: {}", id);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(serviceEntityMapper.toDto(service));
    }

    /**
     * Создать новую услугу.
     *
     * @param masterId   ID мастера-владельца
     * @param serviceDto DTO с данными услуги
     * @return созданная услуга
     */
    @PostMapping
    public ResponseEntity<ServiceEntityDto> createService(@RequestParam Long masterId,
                                                          @Valid @RequestBody ServiceEntityDto serviceDto) {
        log.info("Creating new service for master: {}, name: {}", masterId, serviceDto.getName());
        log.debug("Service details: duration={}, price={}",
                serviceDto.getDurationMinutes(), serviceDto.getPrice());

        Master master = masterRepository.findById(masterId).orElse(null);
        if (master == null) {
            log.warn("Master not found for service creation - masterId: {}", masterId);
            return ResponseEntity.badRequest().build();
        }

        ServiceEntity service = serviceEntityMapper.toEntity(serviceDto);
        service.setMaster(master);
        ServiceEntity saved = serviceEntityService.createService(service);

        log.info("Service created successfully with id: {}", saved.getId());
        return ResponseEntity.ok(serviceEntityMapper.toDto(saved));
    }

    /**
     * Обновить существующую услугу.
     *
     * @param id         ID услуги
     * @param masterId   ID мастера-владельца
     * @param serviceDto DTO с новыми данными
     * @return обновлённая услуга
     */
    @PutMapping("/{id}")
    public ResponseEntity<ServiceEntityDto> updateService(@PathVariable Long id,
                                                          @RequestParam Long masterId,
                                                          @Valid @RequestBody ServiceEntityDto serviceDto) {
        log.info("Updating service: {} for master: {}", id, masterId);

        ServiceEntity existingService = serviceEntityService.getService(id);
        if (existingService == null) {
            log.warn("Service not found for update - id: {}", id);
            return ResponseEntity.notFound().build();
        }

        Master master = masterRepository.findById(masterId).orElse(null);
        if (master == null) {
            log.warn("Master not found for service update - masterId: {}", masterId);
            return ResponseEntity.badRequest().build();
        }

        ServiceEntity service = serviceEntityMapper.toEntity(serviceDto);
        service.setMaster(master);
        service.setId(id);
        ServiceEntity updated = serviceEntityService.updateService(service);

        log.info("Service {} updated successfully", id);
        return ResponseEntity.ok(serviceEntityMapper.toDto(updated));
    }

    /**
     * Удалить услугу. Если есть связанные записи — вернёт 409 Conflict.
     *
     * @param id ID услуги
     * @return 204 No Content при успехе, 409 если есть записи
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        log.info("Deleting service: {}", id);

        try {
            serviceEntityService.deleteService(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("has existing appointments")) {
                log.warn("Service {} has appointments, cannot delete", id);
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            throw e;
        }
    }

    /**
     * Деактивировать услугу (мягкое удаление).
     *
     * @param id ID услуги
     * @return 200 OK
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateService(@PathVariable Long id) {
        log.info("Deactivating service: {}", id);
        serviceEntityService.deactivateService(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Активировать ранее деактивированную услугу.
     *
     * @param id ID услуги
     * @return 200 OK, 404 если услуга не найдена
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateService(@PathVariable Long id) {
        log.info("Activating service: {}", id);
        ServiceEntity service = serviceEntityService.getService(id);
        if (service == null) {
            return ResponseEntity.notFound().build();
        }
        service.setIsActive(true);
        serviceEntityService.updateService(service);
        return ResponseEntity.ok().build();
    }

    /**
     * Получить услуги мастера с пагинацией и сортировкой.
     *
     * @param page    номер страницы (0-based)
     * @param size    размер страницы
     * @param sortBy  поле для сортировки
     * @param sortDir направление сортировки (asc/desc)
     * @param masterId ID мастера
     * @return страница с услугами
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<ServiceEntityDto>> getServicesPaginated(
            @RequestParam(defaultValue = DEFAULT_PAGE + "") int page,
            @RequestParam(defaultValue = DEFAULT_SIZE + "") int size,
            @RequestParam(defaultValue = DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIR) String sortDir,
            @RequestParam Long masterId) {

        log.debug("Fetching services paginated - master: {}, page: {}, size: {}, sortBy: {}, sortDir: {}",
                masterId, page, size, sortBy, sortDir);

        Sort sort = createSort(sortBy, sortDir);

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ServiceEntityDto> services = serviceEntityService.getServicesByMasterId(masterId, pageable);

        log.debug("Found {} services total", services.getTotalElements());
        return ResponseEntity.ok(services);
    }

    /**
     * Получить все услуги мастера.
     *
     * @param masterId ID мастера
     * @return список всех услуг мастера
     */
    @GetMapping("/all")
    public ResponseEntity<List<ServiceEntityDto>> getAllServices(@RequestParam Long masterId) {
        log.debug("Fetching all services DTOs for master: {}", masterId);

        if (!masterRepository.existsById(masterId)) {
            log.warn("Master not found with id: {}", masterId);
            return ResponseEntity.badRequest().build();
        }

        List<ServiceEntityDto> dtos = serviceEntityService.getAllServices(masterId).stream()
                .map(serviceEntityMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Получить услуги мастера по ID (альтернативный эндпоинт).
     *
     * @param masterId ID мастера
     * @return список услуг мастера
     */
    @GetMapping("/by-master/{masterId}")
    public ResponseEntity<List<ServiceEntityDto>> getServicesByMasterId(@PathVariable Long masterId) {
        log.info("GET /api/services/by-master/{}", masterId);

        if (!masterRepository.existsById(masterId)) {
            log.warn("Master not found with id: {}", masterId);
            return ResponseEntity.badRequest().build();
        }

        List<ServiceEntityDto> services = serviceEntityService.getServicesByMasterId(masterId);
        return ResponseEntity.ok(services);
    }

    /**
     * Получить все услуги для администратора с пагинацией.
     *
     * @param page    номер страницы (0-based)
     * @param size    размер страницы
     * @param sortBy  поле для сортировки
     * @param sortDir направление сортировки (asc/desc)
     * @return страница со всеми услугами
     */
    @GetMapping("/admin/all")
    public ResponseEntity<Page<ServiceEntityDto>> getAllServicesForAdmin(
            @RequestParam(defaultValue = DEFAULT_PAGE + "") int page,
            @RequestParam(defaultValue = DEFAULT_SIZE + "") int size,
            @RequestParam(defaultValue = DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = DEFAULT_SORT_DIR) String sortDir) {

        log.info("GET /api/services/admin/all - admin requesting all services");
        Sort sort = createSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(serviceEntityService.getServicesForAdmin(pageable));
    }

    private Sort createSort(String sortBy, String sortDir) {
        if (sortDir.equalsIgnoreCase(SORT_ASC)) {
            return Sort.by(sortBy).ascending();
        } else {
            return Sort.by(sortBy).descending();
        }
    }

    /**
     * Получить материалы, связанные с услугой.
     *
     * @param id ID услуги
     * @return список материалов с количеством использования
     */
    @GetMapping("/{id}/materials")
    public ResponseEntity<List<ServiceMaterialDto>> getServiceMaterials(@PathVariable Long id) {
        return ResponseEntity.ok(serviceEntityService.getMaterialsByServiceId(id));
    }

    /**
     * Добавить материал к услуге.
     *
     * @param id           ID услуги
     * @param materialId   ID материала
     * @param quantityUsed количество используемого материала
     * @param notes        примечания (опционально)
     * @return 200 OK
     */
    @PostMapping("/{id}/materials")
    public ResponseEntity<Void> addMaterialToService(@PathVariable Long id,
                                                     @RequestParam Long materialId,
                                                     @RequestParam BigDecimal quantityUsed,
                                                     @RequestParam(required = false) String notes) {
        serviceEntityService.addMaterialToService(id, materialId, quantityUsed, notes);
        return ResponseEntity.ok().build();
    }

    /**
     * Удалить материал из услуги.
     *
     * @param serviceMaterialId ID связи услуга-материал
     * @return 200 OK
     */
    @DeleteMapping("/materials/{serviceMaterialId}")
    public ResponseEntity<Void> removeMaterialFromService(@PathVariable Long serviceMaterialId) {
        serviceEntityService.removeMaterialFromService(serviceMaterialId);
        return ResponseEntity.ok().build();
    }
}