package com.masterly.core.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
public class AppointmentDto {
    private Long id;
    private Long masterId;
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
}