package com.tennis.court_booking.adapter.in.web.exception;

import com.tennis.court_booking.adapter.in.web.dto.ErrorResponse;
import com.tennis.court_booking.domain.exception.BusinessException;
import com.tennis.court_booking.domain.exception.InvalidTimeSlotException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests exception handling and error response mapping.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("POST", "/api/bookings");
    }

    @Test
    @DisplayName("Should handle BusinessException and return 400 Bad Request")
    void shouldHandleBusinessException() {
        // Given
        BusinessException exception = new BusinessException("Booking cannot start before opening time");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Booking cannot start before opening time", response.getBody().getMessage());
        assertEquals("/api/bookings", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Should handle InvalidTimeSlotException and return 400 Bad Request")
    void shouldHandleInvalidTimeSlotException() {
        // Given
        InvalidTimeSlotException exception = new InvalidTimeSlotException("End time must be after start time");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidTimeSlotException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("End time must be after start time", response.getBody().getMessage());
        assertEquals("/api/bookings", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException and return 400 Bad Request")
    void shouldHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Date cannot be null");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Date cannot be null", response.getBody().getMessage());
        assertEquals("/api/bookings", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Should handle generic Exception and return 500 Internal Server Error")
    void shouldHandleGenericException() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("Unexpected error"));
        assertEquals("/api/bookings", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Should preserve original error message in BusinessException")
    void shouldPreserveOriginalErrorMessageInBusinessException() {
        // Given
        String errorMessage = "The requested time slot overlaps with an existing booking. Requested: [2024-01-15 10:30-11:30], Existing booking ID: 2";
        BusinessException exception = new BusinessException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception, request);

        // Then
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should preserve original error message in InvalidTimeSlotException")
    void shouldPreserveOriginalErrorMessageInInvalidTimeSlotException() {
        // Given
        String errorMessage = "Time slot cannot span multiple days. Start: 2024-01-15T23:30, End: 2024-01-16T00:30";
        InvalidTimeSlotException exception = new InvalidTimeSlotException(errorMessage);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidTimeSlotException(exception, request);

        // Then
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should include request path in error response")
    void shouldIncludeRequestPathInErrorResponse() {
        // Given
        MockHttpServletRequest customRequest = new MockHttpServletRequest("GET", "/api/bookings/123");
        BusinessException exception = new BusinessException("Booking not found");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception, customRequest);

        // Then
        assertNotNull(response.getBody());
        assertEquals("/api/bookings/123", response.getBody().getPath());
    }

    @Test
    @DisplayName("Error response timestamp should be recent")
    void errorResponseTimestampShouldBeRecent() {
        // Given
        BusinessException exception = new BusinessException("Test error");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception, request);

        // Then
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getTimestamp());
        // Timestamp should be within the last second
        assertTrue(response.getBody().getTimestamp().isAfter(
                java.time.LocalDateTime.now().minusSeconds(1)));
    }
}
