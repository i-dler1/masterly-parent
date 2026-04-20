package com.masterly.core.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для передачи данных об услуге.
 * Содержит название, описание, длительность, цену и список материалов.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceEntityDto {

    private Long id;

    private Long masterId;

    @NotBlank(message = "Название процедуры обязательно")
    @Size(min = 2, max = 150, message = "Название процедуры должно быть от 2 до 150 символов")
    private String name;

    private String description;

    @NotNull(message = "Длительность обязательна")
    @Min(value = 5, message = "Длительность не может быть меньше 5 минут")
    @Max(value = 480, message = "Длительность не может быть больше 480 минут (8 часов)")
    private Integer durationMinutes;

    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "0.0", inclusive = false, message = "Цена должна быть больше 0")
    private BigDecimal price;

    private String category;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private List<ServiceMaterialDto> materials;
}