package com.masterly.web.dto;

import lombok.Data;

@Data
public class MasterRegisterRequest {
    private String email;
    private String password;
    private String fullName;
    private String phone;
}