package com.masterly.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для ответа при успешной аутентификации.
 * Содержит JWT токен и базовую информацию о пользователе.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String type;
    private Long id;
    private String email;
    private String name;
    private String role;
}