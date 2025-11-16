package com.tennis.court_booking.application.port.in;

import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Response object representing a successfully created booking.
 * This DTO is returned by the use case to the adapter layer,
 * preventing domain entities from leaking outside the application core.
 */
@Value
public class BookingResponse {
    Long id;
    LocalDate date;
    LocalTime startTime;
    LocalTime endTime;

    /**
     * Creates a new booking response.
     *
     * @param id the unique identifier of the booking
     * @param date the booking date
     * @param startTime the booking start time
     * @param endTime the booking end time
     * @throws IllegalArgumentException if any parameter is null
     */
    public BookingResponse(Long id, LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (id == null) {
            throw new IllegalArgumentException("Booking ID cannot be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("End time cannot be null");
        }
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
