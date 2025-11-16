package com.tennis.court_booking.application.port.in;

import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Command object for reserving a court booking.
 * Represents the input data required to create a new booking.
 * This is an inbound port DTO that carries data from the adapter layer
 * (e.g., REST controller) to the application service.
 */
@Value
public class ReserveCommand {
    LocalDate date;
    LocalTime start;
    LocalTime end;

    /**
     * Creates a new reserve command with validation.
     *
     * @param date the booking date
     * @param start the start time
     * @param end the end time
     * @throws IllegalArgumentException if any parameter is null
     */
    public ReserveCommand(LocalDate date, LocalTime start, LocalTime end) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (start == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        if (end == null) {
            throw new IllegalArgumentException("End time cannot be null");
        }
        this.date = date;
        this.start = start;
        this.end = end;
    }
}
