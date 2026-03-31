package com.masterly.web.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ServiceDto {
    private Long id;
    private Long masterId;
    private String name;
    private String description;
    private Integer durationMinutes;
    private BigDecimal price;
    private String category;
    private Boolean isActive;
}