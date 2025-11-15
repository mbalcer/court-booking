package com.tennis.court_booking.domain.policy;

import com.tennis.court_booking.domain.entity.Booking;
import com.tennis.court_booking.domain.exception.BusinessException;
import com.tennis.court_booking.domain.valueobject.TimeSlot;

import java.util.List;

/**
 * Business policy that validates if a new booking overlaps with existing bookings.
 * Ensures that there are no conflicting reservations for the same time slot.
 */
public class OverlappingReservationsPolicy {

    /**
     * Validates if the given time slot overlaps with any existing bookings.
     *
     * @param timeSlot         the time slot to validate
     * @param existingBookings the list of existing bookings to check against
     * @throws BusinessException if the time slot overlaps with any existing booking
     */
    public void validate(TimeSlot timeSlot, List<Booking> existingBookings) {
        if (timeSlot == null) {
            throw new IllegalArgumentException("TimeSlot cannot be null");
        }
        if (existingBookings == null) {
            throw new IllegalArgumentException("Existing bookings list cannot be null");
        }

        for (Booking booking : existingBookings) {
            if (timeSlot.overlaps(booking.getTimeSlot())) {
                throw new BusinessException(
                    String.format("The requested time slot overlaps with an existing booking. " +
                        "Requested: [%s %s-%s], Existing booking ID: %d [%s %s-%s]",
                        timeSlot.getDate(), timeSlot.getStart(), timeSlot.getEnd(),
                        booking.getId(),
                        booking.getTimeSlot().getDate(),
                        booking.getTimeSlot().getStart(),
                        booking.getTimeSlot().getEnd())
                );
            }
        }
    }
}
