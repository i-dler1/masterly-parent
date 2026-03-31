package com.masterly.core.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AppointmentCreateDto {

    @NotNull(message = "ID мастера обязателен")
    private Long masterId;

    @NotNull(message = "ID клиента обязателен")
    private Long clientId;

    @NotNull(message = "ID услуги обязателен")
    private Long serviceId;

    @NotNull(message = "Дата записи обязательна")
    @FutureOrPresent(message = "Дата записи не может быть в прошлом")
    private LocalDate appointmentDate;

    @NotNull(message = "Время начала обязательно")
    private LocalTime startTime;

    private String notes;
}