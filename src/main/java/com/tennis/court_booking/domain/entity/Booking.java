package com.tennis.court_booking.domain.entity;

import com.tennis.court_booking.domain.valueobject.TimeSlot;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Domain entity representing a court booking.
 * Entity is identified by its id (when persisted).
 * For new bookings not yet persisted, id may be null.
 */
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public class Booking {
    private final Long id;
    private final TimeSlot timeSlot;

    /**
     * Creates a new Booking with the given id and time slot.
     *
     * @param id       the unique identifier of the booking (null for new, unpersisted bookings)
     * @param timeSlot the time slot for the booking
     * @throws IllegalArgumentException if timeSlot is null
     */
    public Booking(Long id, TimeSlot timeSlot) {
        if (timeSlot == null) {
            throw new IllegalArgumentException("TimeSlot cannot be null");
        }

        this.id = id;
        this.timeSlot = timeSlot;
    }
}
