package com.masterly.core.repository;

import com.masterly.core.model.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByMasterId(Long masterId);

    List<Appointment> findByMasterIdAndAppointmentDate(Long masterId, LocalDate date);

    Page<Appointment> findByMasterId(Long masterId, Pageable pageable);

    boolean existsByMasterIdAndAppointmentDateAndStartTime(Long masterId, LocalDate date, LocalTime time);

    List<Appointment> findByClientId(Long clientId);
}