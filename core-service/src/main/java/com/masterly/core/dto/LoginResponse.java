package com.masterly.core.dto;

import com.masterly.core.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String type;
    private Long id;
    private String email;
    private String name;
    private String role;

    public LoginResponse(String token, Long id, String email, String name, String role) {
        this.token = token;
        this.type = "Bearer";
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
    }
}