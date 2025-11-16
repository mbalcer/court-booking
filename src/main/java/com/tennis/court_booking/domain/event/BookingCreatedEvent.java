package com.tennis.court_booking.domain.event;

import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Domain event representing a successful booking creation.
 * This event is raised by the domain when a booking has been successfully created.
 * It represents a fact that has already occurred in the domain.
 *
 * Domain events are part of the ubiquitous language and capture important
 * business events that domain experts care about. They can be used for:
 * - Event sourcing
 * - Integration with other bounded contexts
 * - Triggering side effects (notifications, analytics, etc.)
 *
 * Events are immutable and contain all the information needed by event consumers.
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
