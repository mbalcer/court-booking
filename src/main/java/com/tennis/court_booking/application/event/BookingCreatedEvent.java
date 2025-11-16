package com.tennis.court_booking.application.event;

import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Domain event representing a successful booking creation.
 * This event is published after a booking has been successfully persisted
 * and can be consumed by other systems (e.g., via Kafka) for notifications,
 * analytics, or other downstream processing.
 *
 * Events are immutable and represent facts that have already occurred.
 */
@Value
public class BookingCreatedEvent {
    Long bookingId;
    LocalDate date;
    LocalTime startTime;
    LocalTime endTime;

    /**
     * Creates a new booking created event.
     *
     * @param bookingId the unique identifier of the created booking
     * @param date the booking date
     * @param startTime the booking start time
     * @param endTime the booking end time
     * @throws IllegalArgumentException if any parameter is null
     */
    public BookingCreatedEvent(Long bookingId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (bookingId == null) {
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
        this.bookingId = bookingId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
