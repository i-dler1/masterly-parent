package com.masterly.core.mapper;

import com.masterly.core.dto.MaterialDto;
import com.masterly.core.model.Material;
import com.masterly.core.model.Master;
import org.springframework.stereotype.Component;

@Component
public class MaterialMapper {

    public MaterialDto toDto(Material material) {
        MaterialDto dto = new MaterialDto();
        dto.setId(material.getId());
        dto.setMasterId(material.getMaster().getId());
        dto.setName(material.getName());
        dto.setUnit(material.getUnit());
        dto.setQuantity(material.getQuantity());
        dto.setMinQuantity(material.getMinQuantity());
        dto.setPricePerUnit(material.getPricePerUnit());
        dto.setSupplier(material.getSupplier());
        dto.setNotes(material.getNotes());
        return dto;
    }

    public Material toEntity(MaterialDto dto, Master master) {
        Material material = new Material();
        material.setId(dto.getId());
        material.setMaster(master);
        material.setName(dto.getName());
        material.setUnit(dto.getUnit());
        material.setQuantity(dto.getQuantity());
        material.setMinQuantity(dto.getMinQuantity());
        material.setPricePerUnit(dto.getPricePerUnit());
        material.setSupplier(dto.getSupplier());
        material.setNotes(dto.getNotes());
        return material;
    }
}