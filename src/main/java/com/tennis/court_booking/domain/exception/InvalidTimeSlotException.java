package com.tennis.court_booking.domain.exception;

/**
 * Exception thrown when a TimeSlot is invalid due to validation errors.
 */
public class InvalidTimeSlotException extends RuntimeException {

    public InvalidTimeSlotException(String message) {
        super(message);
    }

    public InvalidTimeSlotException(String message, Throwable cause) {
        super(message, cause);
    }
}
