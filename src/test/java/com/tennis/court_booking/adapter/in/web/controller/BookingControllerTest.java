package com.tennis.court_booking.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tennis.court_booking.adapter.in.web.dto.ReserveBookingRequest;
import com.tennis.court_booking.application.port.in.BookingResponse;
import com.tennis.court_booking.application.port.in.BookingUseCase;
import com.tennis.court_booking.application.port.in.ReserveCommand;
import com.tennis.court_booking.domain.exception.BusinessException;
import com.tennis.court_booking.domain.exception.InvalidTimeSlotException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for BookingController.
 * Tests REST endpoint behavior, request/response mapping, and error handling.
 */
@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingUseCase bookingUseCase;

    @Test
    @DisplayName("Constructor should throw exception when BookingUseCase is null")
    void constructorShouldThrowExceptionWhenUseCaseIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new BookingController(null),
                "BookingUseCase cannot be null");
    }

    @Test
    @DisplayName("POST /api/bookings should create booking and return 201 Created")
    void reserveBookingShouldReturnCreatedStatus() throws Exception {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        ReserveBookingRequest request = new ReserveBookingRequest(date, start, end);
        BookingResponse response = new BookingResponse(1L, date, start, end);

        when(bookingUseCase.reserve(any(ReserveCommand.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.date").value("2024-01-15"))
                .andExpect(jsonPath("$.startTime").value("10:00:00"))
                .andExpect(jsonPath("$.endTime").value("11:00:00"));
    }

    @Test
    @DisplayName("POST /api/bookings should return 400 when BusinessException is thrown")
    void reserveBookingShouldReturnBadRequestOnBusinessException() throws Exception {
        // Given
        ReserveBookingRequest request = new ReserveBookingRequest(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(7, 0),
                LocalTime.of(8, 0)
        );

        when(bookingUseCase.reserve(any(ReserveCommand.class)))
                .thenThrow(new BusinessException("Booking cannot start before opening time"));

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Booking cannot start before opening time"))
                .andExpect(jsonPath("$.path").value("/api/bookings"));
    }

    @Test
    @DisplayName("POST /api/bookings should return 400 when InvalidTimeSlotException is thrown")
    void reserveBookingShouldReturnBadRequestOnInvalidTimeSlotException() throws Exception {
        // Given
        ReserveBookingRequest request = new ReserveBookingRequest(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(11, 0),
                LocalTime.of(10, 0)
        );

        when(bookingUseCase.reserve(any(ReserveCommand.class)))
                .thenThrow(new InvalidTimeSlotException("End time must be after start time"));

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("End time must be after start time"))
                .andExpect(jsonPath("$.path").value("/api/bookings"));
    }

    @Test
    @DisplayName("POST /api/bookings should return 400 when IllegalArgumentException is thrown")
    void reserveBookingShouldReturnBadRequestOnIllegalArgumentException() throws Exception {
        // Given
        ReserveBookingRequest request = new ReserveBookingRequest(
                null,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        when(bookingUseCase.reserve(any(ReserveCommand.class)))
                .thenThrow(new IllegalArgumentException("Date cannot be null"));

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Date cannot be null"))
                .andExpect(jsonPath("$.path").value("/api/bookings"));
    }

    @Test
    @DisplayName("POST /api/bookings should map request to command correctly")
    void reserveBookingShouldMapRequestToCommandCorrectly() throws Exception {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(14, 30);
        LocalTime end = LocalTime.of(15, 30);

        ReserveBookingRequest request = new ReserveBookingRequest(date, start, end);
        BookingResponse response = new BookingResponse(5L, date, start, end);

        when(bookingUseCase.reserve(any(ReserveCommand.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.date").value("2024-01-15"))
                .andExpect(jsonPath("$.startTime").value("14:30:00"))
                .andExpect(jsonPath("$.endTime").value("15:30:00"));
    }

    @Test
    @DisplayName("POST /api/bookings should handle overlapping bookings")
    void reserveBookingShouldHandleOverlappingBookings() throws Exception {
        // Given
        ReserveBookingRequest request = new ReserveBookingRequest(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        when(bookingUseCase.reserve(any(ReserveCommand.class)))
                .thenThrow(new BusinessException("The requested time slot overlaps with an existing booking"));

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("The requested time slot overlaps with an existing booking"));
    }

    @Test
    @DisplayName("POST /api/bookings should include timestamp in error response")
    void reserveBookingShouldIncludeTimestampInErrorResponse() throws Exception {
        // Given
        ReserveBookingRequest request = new ReserveBookingRequest(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(7, 0),
                LocalTime.of(8, 0)
        );

        when(bookingUseCase.reserve(any(ReserveCommand.class)))
                .thenThrow(new BusinessException("Booking cannot start before opening time"));

        // When & Then
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
