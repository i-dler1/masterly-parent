package com.masterly.core.service;

import com.masterly.core.dto.ServiceEntityDto;
import com.masterly.core.mapper.ServiceEntityMapper;
import com.masterly.core.model.ServiceEntity;
import com.masterly.core.repository.ServiceEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceEntityService {

    private final ServiceEntityRepository serviceRepository;
    private final ServiceEntityMapper serviceMapper;

    public List<ServiceEntity> getAllServices(Long masterId) {
        log.debug("Fetching all services for master: {}", masterId);
        return serviceRepository.findByMasterId(masterId);
    }

    public ServiceEntity getService(Long id) {
        log.debug("Fetching service by id: {}", id);
        return serviceRepository.findById(id).orElse(null);
    }

    public ServiceEntity createService(ServiceEntity service) {
        log.info("Creating new service: {}", service.getName());
        log.debug("Service details - name: {}, duration: {}, price: {}",
                service.getName(), service.getDurationMinutes(), service.getPrice());

        ServiceEntity saved = serviceRepository.save(service);
        log.info("Service created successfully with id: {}", saved.getId());

        return saved;
    }

    public ServiceEntity updateService(ServiceEntity service) {
        log.info("Updating service: {}", service.getId());
        log.debug("Updated service details - name: {}, duration: {}, price: {}",
                service.getName(), service.getDurationMinutes(), service.getPrice());

        ServiceEntity updated = serviceRepository.save(service);
        log.info("Service {} updated successfully", service.getId());

        return updated;
    }

    public void deleteService(Long id) {
        log.info("Deleting service: {}", id);
        serviceRepository.deleteById(id);
        log.debug("Service {} deleted", id);
    }

    public Page<ServiceEntityDto> getServicesByMasterId(Long masterId, Pageable pageable) {
        log.debug("Fetching services for master: {}, page: {}, size: {}",
                masterId, pageable.getPageNumber(), pageable.getPageSize());

        Page<ServiceEntity> services = serviceRepository.findByMasterId(masterId, pageable);
        log.debug("Found {} services", services.getTotalElements());

        return services.map(serviceMapper::toDto);
    }

    public List<ServiceEntityDto> getServicesByMasterId(Long masterId) {
        log.debug("Fetching services by master id: {}", masterId);
        List<ServiceEntity> services = serviceRepository.findByMasterId(masterId);
        return services.stream()
                .map(serviceMapper::toDto)
                .collect(Collectors.toList());
    }
}