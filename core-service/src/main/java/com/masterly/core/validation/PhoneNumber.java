package com.masterly.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Аннотация для валидации номера телефона.
 * Проверяет, что номер соответствует формату +375XXXXXXXXX.
 * Использует {@link PhoneNumberValidator} для проверки.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
@Documented
public @interface PhoneNumber {
    String message() default "Неверный формат телефона";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
