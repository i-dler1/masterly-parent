package com.masterly.core.exception;

import lombok.experimental.StandardException;

/**
 * Исключение, выбрасываемое при попытке записи на занятый временной слот.
 */
@StandardException
public class TimeSlotOccupiedException extends RuntimeException {

}