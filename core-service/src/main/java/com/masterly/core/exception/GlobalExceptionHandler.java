package com.masterly.core.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Глобальный обработчик исключений для REST API.
 * Перехватывает исключения и возвращает стандартизированные HTTP-ответы с ошибками.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработка исключения при попытке записи на занятый временной слот.
     *
     * @param e исключение
     * @return ответ с кодом 409 Conflict
     */
    @ExceptionHandler(TimeSlotOccupiedException.class)
    public ResponseEntity<ErrorResponse> handleTimeSlotOccupied(TimeSlotOccupiedException e) {
        log.warn("Time slot occupied: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage(), HttpStatus.CONFLICT.value()));
    }

    /**
     * Обработка исключения при отсутствии запрашиваемого ресурса.
     *
     * @param e исключение
     * @return ответ с кодом 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    /**
     * Обработка исключения при попытке доступа к чужим данным.
     *
     * @param e исключение
     * @return ответ с кодом 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    /**
     * Обработка ошибок валидации входных данных.
     *
     * @param e исключение
     * @return ответ с кодом 400 Bad Request и списком ошибок по полям
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException e) {

        Map<String, List<String>> errors = new HashMap<>();

        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();

            if (!errors.containsKey(fieldName)) {
                errors.put(fieldName, new ArrayList<>());
            }

            errors.get(fieldName).add(errorMessage);
        }
        log.warn("Validation error: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ValidationErrorResponse("Validation failed", HttpStatus.BAD_REQUEST.value(), errors));
    }

    /**
     * Обработка непредвиденных исключений.
     *
     * @param e исключение
     * @return ответ с кодом 500 Internal Server Error
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        log.warn("Unexpected error: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal server error: " + e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    /**
     * Стандартный ответ с ошибкой.
     */
    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
        private int status;
        private LocalDateTime timestamp;

        public ErrorResponse(String message, int status) {
            this(message, status, LocalDateTime.now());
        }
    }

    /**
     * Расширенный ответ для ошибок валидации.
     */
    @Data
    @AllArgsConstructor
    public static class ValidationErrorResponse {
        private String message;
        private int status;
        private Map<String, List<String>> errors;
        private LocalDateTime timestamp;

        public ValidationErrorResponse(String message, int status, Map<String, List<String>> errors) {
            this(message, status, errors, LocalDateTime.now());
        }
    }
}