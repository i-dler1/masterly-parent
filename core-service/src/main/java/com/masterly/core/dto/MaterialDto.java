package com.masterly.core.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO для передачи данных о расходном материале.
 * Содержит информацию о количестве, единице измерения и цене.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDto {
    private Long id;

    private Long masterId;

    @NotBlank(message = "Название материала обязательно")
    @Size(min = 2, max = 150, message = "Название материала должно быть от 2 до 150 символов")
    private String name;

    @NotBlank(message = "Единица измерения обязательна")
    @Size(min = 1, max = 10, message = "Единица измерения должна быть от 1 до 10 символов")
    private String unit;

    @NotNull(message = "Количество обязательно")
    @DecimalMin(value = "0.0", message = "Количество не может быть отрицательным")
    private BigDecimal quantity;

    @DecimalMin(value = "0.0", message = "Минимальное количество не может быть отрицательным")
    private BigDecimal minQuantity;

    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "0.0", inclusive = false, message = "Цена должна быть больше 0")
    private BigDecimal pricePerUnit;

    private String supplier;

    private String notes;
}