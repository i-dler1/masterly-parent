package com.masterly.core.dto;

import com.masterly.core.validation.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для передачи данных о клиенте.
 * Содержит контактную информацию и признак постоянного клиента.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {

    private Long id;

    @NotBlank(message = "Имя обязательно")
    @Size(min = 2, max = 150, message = "Имя должно быть от 2 до 150 символов")
    private String fullName;

    @PhoneNumber
    @Pattern(regexp = "^\\+375\\d{9}$", message = "Телефон должен быть в формате +375XXXXXXXXX")
    private String phone;

    @Email(message = "Email должен быть корректным")
    private String email;

    private String instagram;
    private String telegram;
    private String notes;
    private Boolean isRegular;
    private LocalDateTime lastAppointmentDate;
}