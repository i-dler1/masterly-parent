package com.masterly.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Аннотация для валидации временного диапазона.
 * Проверяет, что время начала раньше времени окончания.
 * Использует {@link TimeRangeValidator} для проверки.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimeRangeValidator.class)
@Documented
public @interface ValidTimeRange {
    String message() default "Время начала не может быть позже времени окончания";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
