package com.masterly.core.repository;

import com.masterly.core.model.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    List<AvailabilitySlot> findByMasterIdAndSlotDateAndIsBookedFalse(Long masterId, LocalDate date);

    Optional<AvailabilitySlot> findByMasterIdAndSlotDateAndStartTimeAndIsBookedFalse(
            Long masterId, LocalDate date, LocalTime startTime);

    List<AvailabilitySlot> findByServiceIdAndSlotDateAndIsBookedFalse(Long serviceId, LocalDate date);
}