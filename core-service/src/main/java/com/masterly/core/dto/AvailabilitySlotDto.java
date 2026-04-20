package com.masterly.core.dto;

import com.masterly.core.validation.ValidTimeRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO для передачи данных о слоте доступности мастера.
 * Используется в REST API для отображения и создания слотов.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidTimeRange
public class AvailabilitySlotDto {
    private Long id;
    private Long masterId;
    private String masterName;
    private Long serviceId;
    private String serviceName;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isBooked;
}