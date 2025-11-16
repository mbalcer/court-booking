package com.tennis.court_booking.adapter.in.web.exception;

import com.tennis.court_booking.adapter.in.web.dto.ErrorResponse;
import com.tennis.court_booking.domain.exception.BusinessException;
import com.tennis.court_booking.domain.exception.InvalidTimeSlotException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Global exception handler for REST API endpoints.
 * Translates domain exceptions into appropriate HTTP error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles business rule violations from the domain layer.
     * Business exceptions are mapped to HTTP 400 (Bad Request) status.
     *
     * @param ex the business exception
     * @param request the HTTP request
     * @return error response with 400 status
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles invalid time slot validation errors.
     * Invalid time slot exceptions are mapped to HTTP 400 (Bad Request) status.
     *
     * @param ex the invalid time slot exception
     * @param request the HTTP request
     * @return error response with 400 status
     */
    @ExceptionHandler(InvalidTimeSlotException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTimeSlotException(
            InvalidTimeSlotException ex,
            HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles illegal argument exceptions (typically from null validations).
     * Illegal argument exceptions are mapped to HTTP 400 (Bad Request) status.
     *
     * @param ex the illegal argument exception
     * @param request the HTTP request
     * @return error response with 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handles all other unexpected exceptions.
     * Generic exceptions are mapped to HTTP 500 (Internal Server Error) status.
     *
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred: " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
