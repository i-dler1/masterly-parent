package com.masterly.core.validation;

import com.masterly.core.repository.MasterRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

/**
 * Валидатор для аннотации {@link UniqueEmail}.
 * Проверяет, что email не занят другим пользователем в базе данных.
 */
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private final MasterRepository masterRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return true;
        }
        return !masterRepository.existsByEmail(email);
    }
}
