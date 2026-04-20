package com.masterly.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * DTO для передачи данных о записи.
 * Используется в REST API для отображения информации о записи.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDto {
    private Long id;
    private Long masterId;
    private String masterName;
    private Long clientId;
    private String clientName;
    private Long serviceId;
    private String serviceName;
    private Integer durationMinutes;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean justCreated;
}