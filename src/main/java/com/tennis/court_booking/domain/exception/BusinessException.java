package com.tennis.court_booking.domain.exception;

/**
 * Exception thrown when a business rule or policy is violated.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
