package com.masterly.core.entity;

/**
 * Статусы записи на услугу.
 *
 * <ul>
 *   <li>{@link #PENDING} — ожидает подтверждения</li>
 *   <li>{@link #CONFIRMED} — подтверждена мастером</li>
 *   <li>{@link #COMPLETED} — услуга выполнена</li>
 *   <li>{@link #CANCELLED} — запись отменена</li>
 * </ul>
 */
public enum AppointmentStatus {

    /** Ожидает подтверждения */
    PENDING,

    /** Подтверждено мастером */
    CONFIRMED,

    /** Услуга выполнена */
    COMPLETED,

    /** Запись отменена */
    CANCELLED
}