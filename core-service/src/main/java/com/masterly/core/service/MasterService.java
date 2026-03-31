package com.masterly.core.service;

import com.masterly.core.dto.MasterCreateDto;
import com.masterly.core.dto.MasterDto;
import com.masterly.core.dto.MasterUpdateDto;
import com.masterly.core.mapper.MasterMapper;
import com.masterly.core.model.Master;
import com.masterly.core.model.Role;
import com.masterly.core.repository.MasterRepository;
import com.masterly.core.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MasterService {

    private final MasterRepository masterRepository;
    private final MasterMapper masterMapper;
    private final RoleRepository roleRepository;

    public Master findByEmail(String email) {
        log.debug("Finding master by email: {}", email);

        return masterRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Master not found with email: {}", email);
                    return new RuntimeException("Master not found with email: " + email);
                });
    }

    public MasterDto register(MasterCreateDto createDto) {
        log.info("Registering new master with email: {}", createDto.getEmail());

        // Создаем объект Role для мастера
        Role masterRole = new Role();
        masterRole.setId(2L); // ID роли MASTER
        masterRole.setName("MASTER");

        Master master = new Master();
        master.setEmail(createDto.getEmail());
        master.setPasswordHash(createDto.getPassword());
        master.setFullName(createDto.getFullName());
        master.setRole(masterRole);
        master.setIsActive(true);

        Master saved = masterRepository.save(master);
        log.info("Master registered successfully with id: {}", saved.getId());

        return masterMapper.toDto(saved);
    }

    public MasterDto getMasterById(Long id) {
        log.debug("Fetching master by id: {}", id);

        Master master = masterRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Master not found with id: {}", id);
                    return new RuntimeException("Master not found");
                });

        log.debug("Master found: {}", master.getEmail());
        return masterMapper.toDto(master);
    }

    public MasterDto updateMaster(Long id, MasterUpdateDto updateDto) {
        log.info("Updating master with id: {}", id);

        Master master = masterRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Master not found for update - id: {}", id);
                    return new RuntimeException("Master not found");
                });

        log.debug("Updating master details - name: {}, phone: {}, business: {}, specialization: {}",
                updateDto.getFullName(), updateDto.getPhone(),
                updateDto.getBusinessName(), updateDto.getSpecialization());

        master.setFullName(updateDto.getFullName());
        master.setPhone(updateDto.getPhone());
        master.setBusinessName(updateDto.getBusinessName());
        master.setSpecialization(updateDto.getSpecialization());

        Master updated = masterRepository.save(master);
        log.info("Master {} updated successfully", id);

        return masterMapper.toDto(updated);
    }

    public List<MasterDto> getAllMasters() {
        log.debug("Fetching all masters (users with MASTER role)");

        // Находим роль MASTER
        Role masterRole = roleRepository.findByName("MASTER")
                .orElseThrow(() -> new RuntimeException("Role MASTER not found"));

        // Получаем всех мастеров с ролью MASTER
        List<Master> masters = masterRepository.findByRole(masterRole);

        return masters.stream()
                .map(masterMapper::toDto)
                .collect(Collectors.toList());
    }
}