package com.masterly.core.mapper;

import com.masterly.core.dto.AppointmentCreateDto;
import com.masterly.core.dto.AppointmentDto;
import com.masterly.core.entity.Appointment;
import com.masterly.core.entity.Client;
import com.masterly.core.entity.Master;
import com.masterly.core.entity.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Маппер для преобразования между сущностью {@link Appointment}
 * и DTO {@link AppointmentDto} / {@link AppointmentCreateDto}.
 * Использует MapStruct для автоматической генерации реализации.
 */
@Mapper(componentModel = "spring")
public interface AppointmentMapper {

    /**
     * Преобразовать сущность {@link Appointment} в {@link AppointmentDto}.
     *
     * @param appointment сущность записи
     * @return DTO с данными записи
     */
    @Mapping(target = "masterId", source = "master.id")
    @Mapping(target = "masterName", source = "master.fullName")
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "clientName", source = "client.fullName")
    @Mapping(target = "serviceName", source = "service.name")
    @Mapping(target = "durationMinutes", source = "service.durationMinutes")
    @Mapping(target = "status", expression = "java(appointment.getStatus().name())")
    @Mapping(target = "justCreated", ignore = true)
    AppointmentDto toDto(Appointment appointment);

    /**
     * Преобразовать {@link AppointmentCreateDto} в сущность {@link Appointment}.
     *
     * @param dto     DTO с данными для создания записи
     * @param master  мастер
     * @param client  клиент
     * @param service услуга
     * @return сущность записи
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "master", source = "master")
    @Mapping(target = "client", source = "client")
    @Mapping(target = "service", source = "service")
    @Mapping(target = "notes", source = "dto.notes")
    @Mapping(target = "appointmentDate", source = "dto.appointmentDate")
    @Mapping(target = "startTime", source = "dto.startTime")
    @Mapping(target = "endTime", ignore = true)  // ← ИГНОРИРУЕМ, так как его нет в DTO
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Appointment toEntity(AppointmentCreateDto dto, Master master, Client client, ServiceEntity service);
}