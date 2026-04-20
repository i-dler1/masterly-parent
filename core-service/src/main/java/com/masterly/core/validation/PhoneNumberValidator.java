package com.masterly.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Валидатор для аннотации {@link PhoneNumber}.
 * Проверяет, что строка соответствует формату белорусского номера телефона (+375XXXXXXXXX).
 */
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        return phone.matches("^\\+375\\d{9}$");
    }
}
