package com.masterly.core.validation;

import com.masterly.core.dto.AvailabilitySlotDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Валидатор для аннотации {@link ValidTimeRange}.
 * Проверяет, что время начала раньше времени окончания.
 */
public class TimeRangeValidator implements ConstraintValidator<ValidTimeRange, AvailabilitySlotDto> {
    @Override
    public boolean isValid(AvailabilitySlotDto availabilitySlotDto, ConstraintValidatorContext constraintValidatorContext) {
        if (availabilitySlotDto.getStartTime() == null || availabilitySlotDto.getEndTime() == null) {
            return true;
        }
        return availabilitySlotDto.getStartTime().isBefore(availabilitySlotDto.getEndTime());
    }
}
