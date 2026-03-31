package com.masterly.core.mapper;

import com.masterly.core.dto.MasterDto;
import com.masterly.core.model.Master;
import com.masterly.core.model.Role;
import com.masterly.core.repository.RoleRepository;
import org.springframework.stereotype.Component;

@Component
public class MasterMapper {

    private final RoleRepository roleRepository;

    public MasterMapper(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public MasterDto toDto(Master master) {
        if (master == null) {
            return null;
        }

        MasterDto dto = new MasterDto();
        dto.setId(master.getId());
        dto.setEmail(master.getEmail());
        dto.setFullName(master.getFullName());
        dto.setPhone(master.getPhone());
        dto.setBusinessName(master.getBusinessName());
        dto.setSpecialization(master.getSpecialization());
        dto.setAvatarUrl(master.getAvatarUrl());
        dto.setIsActive(master.getIsActive());
        dto.setRole(master.getRole() != null ? master.getRole().getName() : null);
        dto.setCreatedAt(master.getCreatedAt());
        dto.setUpdatedAt(master.getUpdatedAt());

        return dto;
    }

    public Master toEntity(MasterDto dto) {
        if (dto == null) {
            return null;
        }

        Master master = new Master();
        master.setId(dto.getId());
        master.setEmail(dto.getEmail());
        master.setFullName(dto.getFullName());
        master.setPhone(dto.getPhone());
        master.setBusinessName(dto.getBusinessName());
        master.setSpecialization(dto.getSpecialization());
        master.setAvatarUrl(dto.getAvatarUrl());
        master.setIsActive(dto.getIsActive());
        // Преобразуем String → Role
        if (dto.getRole() != null) {
            Role role = roleRepository.findByName(dto.getRole())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + dto.getRole()));
            master.setRole(role);
        }
        master.setCreatedAt(dto.getCreatedAt());
        master.setUpdatedAt(dto.getUpdatedAt());

        return master;
    }
}