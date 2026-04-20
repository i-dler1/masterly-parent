package com.masterly.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Сущность связи услуги и материала.
 * Определяет, какие материалы и в каком количестве используются для оказания услуги.
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "service_materials")
public class ServiceMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Column(name = "quantity_used", nullable = false)
    private BigDecimal quantityUsed;

    private String notes;
}