package com.masterly.core.dto;

import com.masterly.core.model.Role;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MasterDto {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String businessName;
    private String specialization;
    private String avatarUrl;
    private Boolean isActive;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}