package com.tennis.court_booking.domain.valueobject;

import lombok.Value;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Value object representing a time slot for court booking.
 * Immutable and contains validation logic.
 */
@Value
public class TimeSlot {
    LocalDate date;
    LocalTime start;
    LocalTime end;

    /**
     * Creates a new TimeSlot with validation.
     *
     * @param date  the date of the booking
     * @param start the start time of the booking
     * @param end   the end time of the booking
     * @throws IllegalArgumentException if validation fails
     */
    public TimeSlot(LocalDate date, LocalTime start, LocalTime end) {
        validateNotNull(date, start, end);
        validateEndAfterStart(start, end);

        this.date = date;
        this.start = start;
        this.end = end;
    }

    private void validateNotNull(LocalDate date, LocalTime start, LocalTime end) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (start == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        if (end == null) {
            throw new IllegalArgumentException("End time cannot be null");
        }
    }

    private void validateEndAfterStart(LocalTime start, LocalTime end) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    /**
     * Checks if this time slot overlaps with another time slot.
     * Two time slots overlap if they are on the same date and their time ranges intersect.
     *
     * @param other the other time slot to check
     * @return true if the time slots overlap, false otherwise
     */
    public boolean overlaps(TimeSlot other) {
        if (other == null) {
            return false;
        }

        // Different dates cannot overlap
        if (!this.date.equals(other.date)) {
            return false;
        }

        // Check if time ranges overlap
        // Two ranges [start1, end1] and [start2, end2] overlap if:
        // start1 < end2 AND start2 < end1
        return this.start.isBefore(other.end) && other.start.isBefore(this.end);
    }
}
