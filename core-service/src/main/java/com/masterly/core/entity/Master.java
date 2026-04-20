package com.masterly.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * Сущность "Мастер".
 * Представляет собой мастера услуг сфер обслуживания, который может:
 * 1. входить в систему;
 * 2. управлять своими услугами, материалами, клиентами и записями.
 * Содержит информацию о мастере: email, ФИО, телефон, специализация.
 * Используется для аутентификации и авторизации.
 * Реализует интерфейс {@link org.springframework.security.core.userdetails.UserDetails}
 * для интеграции с Spring Security.
 */
@Data

@NoArgsConstructor
@Entity
@Table(name = "masters")
public class Master implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(name = "business_name", length = 200)
    private String businessName;

    private String specialization;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Проверить, является ли пользователь администратором.
     *
     * @return true если роль ADMIN
     */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    /**
     * Проверить, является ли пользователь мастером.
     *
     * @return true если роль MASTER
     */
    public boolean isMaster() {
        return "MASTER".equals(role);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive != null && isActive;
    }
}