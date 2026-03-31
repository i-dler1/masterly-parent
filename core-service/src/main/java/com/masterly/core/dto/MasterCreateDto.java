package com.masterly.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MasterCreateDto {

    @NotBlank(message = "Email обязателен")
    @Email(message = "Email должен быть корректным")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 3, max = 100, message = "Пароль должен быть от 3 до 100 символов")
    private String password;

    @NotBlank(message = "Имя обязательно")
    @Size(min = 2, max = 150, message = "Имя должно быть от 2 до 150 символов")
    private String fullName;

    @NotBlank(message = "Номер телефона обязательно")
    @Pattern(regexp = "^\\+375\\d{9}$", message = "Телефон должен быть в формате +375XXXXXXXXX")
    private String phone;
}