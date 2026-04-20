package com.masterly.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Аннотация для валидации уникальности email.
 * Проверяет, что email не занят другим пользователем.
 * Использует {@link UniqueEmailValidator} для проверки.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
@Documented
public @interface UniqueEmail {

    /**
     * Сообщение об ошибке при нарушении уникальности.
     *
     * @return текст ошибки
     */
    String message() default "Email уже используется";

    /**
     * Группы валидации.
     *
     * @return массив групп
     */
    Class<?>[] groups() default {};

    /**
     * Дополнительная нагрузка (payload).
     *
     * @return массив payload классов
     */
    Class<? extends Payload>[] payload() default {};
}
