package com.tennis.court_booking.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * REST API response DTO for a successfully created booking.
 * This is the adapter-layer DTO that will be serialized to JSON in HTTP responses.
 * It is mapped from BookingResponse returned by the application layer.
 */
@Getter
@AllArgsConstructor
public class ReserveBookingResponse {
    private final Long id;
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
}
