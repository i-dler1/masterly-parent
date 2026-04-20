package com.masterly.core.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для обновления профиля мастера.
 * Содержит поля, доступные для редактирования.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasterUpdateDto {

    @NotBlank(message = "Имя обязательно")
    @Size(min = 2, max = 150, message = "Имя должно быть от 2 до 150 символов")
    private String fullName;

    @Pattern(regexp = "^\\+375\\d{9}$", message = "Телефон должен быть в формате +375XXXXXXXXX")
    private String phone;

    private String businessName;
    private String specialization;
}