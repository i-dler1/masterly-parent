package com.masterly.core.repository;

import com.masterly.core.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Репозиторий для работы с сущностью {@link AvailabilitySlot}.
 * Предоставляет методы для поиска слотов доступности по различным критериям.
 */
public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    /**
     * Найти свободные слоты мастера на указанную дату.
     *
     * @param masterId ID мастера
     * @param date     дата
     * @return список свободных слотов
     */
    List<AvailabilitySlot> findByMasterIdAndSlotDateAndIsBookedFalse(Long masterId, LocalDate date);

    /**
     * Найти свободные слоты мастера для услуги за период.
     *
     * @param masterId  ID мастера
     * @param serviceId ID услуги
     * @param startDate начальная дата
     * @param endDate   конечная дата
     * @return список свободных слотов
     */
    List<AvailabilitySlot> findByMasterIdAndServiceIdAndSlotDateBetweenAndIsBookedFalse(
            Long masterId, Long serviceId, LocalDate startDate, LocalDate endDate);

    /**
     * Найти свободные слоты мастера для услуги на конкретную дату.
     *
     * @param masterId  ID мастера
     * @param serviceId ID услуги
     * @param slotDate  дата
     * @return список свободных слотов
     */
    List<AvailabilitySlot> findByMasterIdAndServiceIdAndSlotDateAndIsBookedFalse(
            Long masterId, Long serviceId, LocalDate slotDate);

    /**
     * Найти все слоты мастера.
     *
     * @param masterId ID мастера
     * @return список всех слотов мастера
     */
    List<AvailabilitySlot> findByMasterId(Long masterId);

    /**
     * Найти все слоты на указанную дату.
     *
     * @param localDate дата
     * @return список слотов
     */
    List<AvailabilitySlot> findBySlotDate(LocalDate localDate);

    /**
     * Найти слоты мастера на дату и время начала.
     *
     * @param masterId  ID мастера
     * @param slotDate  дата
     * @param startTime время начала
     * @return список слотов
     */
    List<AvailabilitySlot> findByMasterIdAndSlotDateAndStartTime(Long masterId, LocalDate slotDate, LocalTime startTime);
}