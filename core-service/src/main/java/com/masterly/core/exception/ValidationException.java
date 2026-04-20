package com.masterly.core.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * Исключение, выбрасываемое при ошибках валидации данных.
 * Содержит ошибки, сгруппированные по полям: название поля → список сообщений.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ValidationException extends RuntimeException {

    /**
     * Ошибки валидации, сгруппированные по полям
     */
    private Map<String, List<String>> errors;

    /**
     * Создать исключение с указанным сообщением.
     *
     * @param message сообщение об ошибке
     */
    public ValidationException(String message) {
        super(message);
        this.errors = null;
    }
}
