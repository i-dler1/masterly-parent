package com.masterly.web.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentCreateDto {
    private Long masterId;
    private Long clientId;
    private Long serviceId;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private String notes;
}