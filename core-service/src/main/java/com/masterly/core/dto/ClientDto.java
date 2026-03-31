package com.masterly.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClientDto {

    private Long id;

    @NotBlank(message = "Имя обязательно")
    @Size(min = 2, max = 150, message = "Имя должно быть от 2 до 150 символов")
    private String fullName;

    @Pattern(regexp = "^\\+375\\d{9}$", message = "Телефон должен быть в формате +375XXXXXXXXX")
    private String phone;

    @Email(message = "Email должен быть корректным")
    private String email;
}