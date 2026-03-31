package com.masterly.core.exception;

public class TimeSlotOccupiedException extends RuntimeException {
    public TimeSlotOccupiedException(String message) {
        super(message);
    }
}