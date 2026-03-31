package com.masterly.core.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
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