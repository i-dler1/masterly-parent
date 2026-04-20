package com.masterly.core.service;

import com.masterly.core.dto.ServiceEntityDto;
import com.masterly.core.dto.ServiceMaterialDto;
import com.masterly.core.mapper.ServiceEntityMapper;
import com.masterly.core.entity.Appointment;
import com.masterly.core.entity.Material;
import com.masterly.core.entity.ServiceEntity;
import com.masterly.core.entity.ServiceMaterial;
import com.masterly.core.repository.AppointmentRepository;
import com.masterly.core.repository.MaterialRepository;
import com.masterly.core.repository.ServiceEntityRepository;
import com.masterly.core.repository.ServiceMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления услугами.
 * Предоставляет бизнес-логику для создания, обновления, удаления услуг,
 * а также управления материалами услуги.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceEntityService {

    private final ServiceEntityRepository serviceEntityRepository;
    private final ServiceEntityMapper serviceEntityMapper;
    private final AppointmentRepository appointmentRepository;
    private final ServiceMaterialRepository serviceMaterialRepository;
    private final MaterialRepository materialRepository;

    /**
     * Получить все услуги мастера.
     *
     * @param masterId ID мастера
     * @return список услуг мастера
     */
    public List<ServiceEntity> getAllServices(Long masterId) {
        log.debug("Fetching all services for master: {}", masterId);
        return serviceEntityRepository.findByMasterId(masterId);
    }

    /**
     * Получить услугу по ID.
     *
     * @param id ID услуги
     * @return услуга или null если не найдена
     */
    public ServiceEntity getService(Long id) {
        log.debug("Fetching service by id: {}", id);
        return serviceEntityRepository.findById(id).orElse(null);
    }

    /**
     * Создать новую услугу.
     *
     * @param service услуга для создания
     * @return созданная услуга
     */
    public ServiceEntity createService(ServiceEntity service) {
        log.info("Creating new service: {}", service.getName());
        log.debug("Service details - name: {}, duration: {}, price: {}",
                service.getName(), service.getDurationMinutes(), service.getPrice());

        ServiceEntity saved = serviceEntityRepository.save(service);
        log.info("Service created successfully with id: {}", saved.getId());

        return saved;
    }

    /**
     * Обновить существующую услугу.
     *
     * @param service услуга с обновлёнными данными
     * @return обновлённая услуга
     */
    public ServiceEntity updateService(ServiceEntity service) {
        log.info("Updating service: {}", service.getId());
        log.debug("Updated service details - name: {}, duration: {}, price: {}",
                service.getName(), service.getDurationMinutes(), service.getPrice());

        ServiceEntity updated = serviceEntityRepository.save(service);
        log.info("Service {} updated successfully", service.getId());

        return updated;
    }

    /**
     * Удалить услугу. Если есть связанные записи — выбрасывает исключение.
     *
     * @param id ID услуги
     * @throws RuntimeException если у услуги есть записи
     */
    public void deleteService(Long id) {
        log.info("Deleting service: {}", id);

        // Проверяем, есть ли записи на эту услугу
        List<Appointment> appointments = appointmentRepository.findByServiceId(id);
        if (!appointments.isEmpty()) {
            log.warn("Cannot delete service {} - has existing appointments, deactivating instead", id);
            throw new RuntimeException("Service has existing appointments");
        }

        serviceEntityRepository.deleteById(id);
        log.debug("Service {} deleted", id);
    }

    /**
     * Деактивировать услугу (мягкое удаление).
     *
     * @param id ID услуги
     * @throws RuntimeException если услуга не найдена
     */
    public void deactivateService(Long id) {
        log.info("Deactivating service: {}", id);
        ServiceEntity service = getService(id);
        if (service == null) {
            throw new RuntimeException("Service not found");
        }
        service.setIsActive(false);
        serviceEntityRepository.save(service);
        log.info("Service {} deactivated", id);
    }

    /**
     * Активировать ранее деактивированную услугу.
     *
     * @param id ID услуги
     * @throws RuntimeException если услуга не найдена
     */
    public void activateService(Long id) {
        log.info("Activating service: {}", id);
        ServiceEntity service = getService(id);
        if (service == null) {
            throw new RuntimeException("Service not found");
        }
        service.setIsActive(true);
        serviceEntityRepository.save(service);
    }

    /**
     * Получить услуги мастера с пагинацией.
     *
     * @param masterId ID мастера
     * @param pageable параметры пагинации
     * @return страница с услугами
     */
    public Page<ServiceEntityDto> getServicesByMasterId(Long masterId, Pageable pageable) {
        log.debug("Fetching services for master: {}, page: {}, size: {}",
                masterId, pageable.getPageNumber(), pageable.getPageSize());

        Page<ServiceEntity> services = serviceEntityRepository.findByMasterId(masterId, pageable);
        log.debug("Found {} services", services.getTotalElements());

        return services.map(serviceEntityMapper::toDto);
    }

    /**
     * Получить услуги мастера в виде списка DTO.
     *
     * @param masterId ID мастера
     * @return список DTO услуг мастера
     */
    public List<ServiceEntityDto> getServicesByMasterId(Long masterId) {
        log.debug("Fetching services by master id: {}", masterId);
        List<ServiceEntity> services = serviceEntityRepository.findByMasterId(masterId);
        return services.stream()
                .map(serviceEntityMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить все услуги для администратора с пагинацией.
     *
     * @param pageable параметры пагинации
     * @return страница со всеми услугами
     */
    public Page<ServiceEntityDto> getServicesForAdmin(Pageable pageable) {
        log.debug("Fetching all services for admin");
        Page<ServiceEntity> services = serviceEntityRepository.findAll(pageable);
        return services.map(serviceEntityMapper::toDto);
    }

    /**
     * Получить все услуги мастера в виде списка DTO (альтернативный метод).
     *
     * @param masterId ID мастера
     * @return список DTO услуг мастера
     */
    public List<ServiceEntityDto> getAllServicesByMasterId(Long masterId) {
        List<ServiceEntity> services = serviceEntityRepository.findByMasterId(masterId);
        return serviceEntityMapper.toDtoList(services);
    }

    /**
     * Получить материалы, связанные с услугой.
     *
     * @param serviceId ID услуги
     * @return список материалов с количеством использования
     */
    public List<ServiceMaterialDto> getMaterialsByServiceId(Long serviceId) {
        List<ServiceMaterial> serviceMaterials = serviceMaterialRepository.findByServiceId(serviceId);
        return serviceMaterials.stream()
                .map(sm -> ServiceMaterialDto.builder()
                        .id(sm.getId())
                        .serviceId(sm.getService().getId())
                        .materialId(sm.getMaterial().getId())
                        .materialName(sm.getMaterial().getName())
                        .quantityUsed(sm.getQuantityUsed())
                        .notes(sm.getNotes())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Добавить материал к услуге.
     *
     * @param serviceId    ID услуги
     * @param materialId   ID материала
     * @param quantityUsed количество используемого материала
     * @param notes        примечания
     * @throws RuntimeException если услуга или материал не найдены
     */
    public void addMaterialToService(Long serviceId, Long materialId, BigDecimal quantityUsed, String notes) {
        ServiceEntity service = getService(serviceId);
        if (service == null) {
            throw new RuntimeException("Service not found");
        }
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        ServiceMaterial serviceMaterial = new ServiceMaterial();
        serviceMaterial.setService(service);
        serviceMaterial.setMaterial(material);
        serviceMaterial.setQuantityUsed(quantityUsed);
        serviceMaterial.setNotes(notes);

        serviceMaterialRepository.save(serviceMaterial);
    }

    /**
     * Удалить материал из услуги.
     *
     * @param serviceMaterialId ID связи услуга-материал
     */
    public void removeMaterialFromService(Long serviceMaterialId) {
        serviceMaterialRepository.deleteById(serviceMaterialId);
    }
}