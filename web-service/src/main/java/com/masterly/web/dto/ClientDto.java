package com.masterly.web.dto;

import lombok.Data;

@Data
public class ClientDto {
    private Long id;
    private Long masterId;
    private String fullName;
    private String phone;
    private String email;
}