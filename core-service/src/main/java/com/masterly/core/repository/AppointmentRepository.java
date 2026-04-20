package com.masterly.core.repository;

import com.masterly.core.entity.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link Appointment}.
 * Предоставляет методы для поиска записей по мастеру, клиенту, услуге и дате.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Найти все записи мастера.
     *
     * @param masterId ID мастера
     * @return список записей мастера
     */
    List<Appointment> findByMasterId(Long masterId);

    /**
     * Найти записи мастера на указанную дату.
     *
     * @param masterId ID мастера
     * @param date     дата
     * @return список записей
     */
    List<Appointment> findByMasterIdAndAppointmentDate(Long masterId, LocalDate date);

    /**
     * Найти записи мастера с пагинацией.
     *
     * @param masterId ID мастера
     * @param pageable параметры пагинации
     * @return страница с записями мастера
     */
    Page<Appointment> findByMasterId(Long masterId, Pageable pageable);

    /**
     * Проверить существование записи на указанные дату и время.
     *
     * @param masterId ID мастера
     * @param date     дата
     * @param time     время начала
     * @return true если запись существует
     */
    boolean existsByMasterIdAndAppointmentDateAndStartTime(Long masterId, LocalDate date, LocalTime time);

    /**
     * Найти все записи клиента.
     *
     * @param clientId ID клиента
     * @return список записей клиента
     */
    List<Appointment> findByClientId(Long clientId);

    /**
     * Найти все записи для указанной услуги.
     *
     * @param serviceId ID услуги
     * @return список записей
     */
    List<Appointment> findByServiceId(Long serviceId);
}