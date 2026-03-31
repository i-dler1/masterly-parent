package com.masterly.core.mapper;

import com.masterly.core.dto.AppointmentCreateDto;
import com.masterly.core.dto.AppointmentDto;
import com.masterly.core.model.Appointment;
import com.masterly.core.model.AppointmentStatus;
import com.masterly.core.model.Master;
import com.masterly.core.model.Client;
import com.masterly.core.model.ServiceEntity;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class AppointmentMapper {

    public AppointmentDto toDto(Appointment appointment) {
        AppointmentDto dto = new AppointmentDto();
        dto.setId(appointment.getId());
        dto.setMasterId(appointment.getMaster().getId());
        dto.setClientId(appointment.getClient().getId());
        dto.setClientName(appointment.getClient().getFullName());
        dto.setServiceId(appointment.getService().getId());
        dto.setServiceName(appointment.getService().getName());
        dto.setDurationMinutes(appointment.getService().getDurationMinutes());
        dto.setAppointmentDate(appointment.getAppointmentDate());
        dto.setStartTime(appointment.getStartTime());
        dto.setEndTime(appointment.getEndTime());
        dto.setStatus(appointment.getStatus().name());
        dto.setNotes(appointment.getNotes());
        dto.setCreatedAt(appointment.getCreatedAt());
        dto.setUpdatedAt(appointment.getUpdatedAt());
        return dto;
    }

    public Appointment toEntity(AppointmentCreateDto dto, Master master, Client client, ServiceEntity service) {
        Appointment appointment = new Appointment();
        appointment.setMaster(master);
        appointment.setClient(client);
        appointment.setService(service);
        appointment.setAppointmentDate(dto.getAppointmentDate());
        appointment.setStartTime(dto.getStartTime());
        // Вычисляем время окончания: startTime + длительность услуги (в минутах)
        LocalTime endTime = dto.getStartTime().plusMinutes(service.getDurationMinutes());
        appointment.setEndTime(endTime);
        appointment.setNotes(dto.getNotes());
        appointment.setStatus(AppointmentStatus.PENDING);
        return appointment;
    }
}