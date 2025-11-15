package com.tennis.court_booking.domain.service;

import com.tennis.court_booking.domain.entity.Booking;
import com.tennis.court_booking.domain.exception.BusinessException;
import com.tennis.court_booking.domain.policy.OpeningHoursPolicy;
import com.tennis.court_booking.domain.policy.OverlappingReservationsPolicy;
import com.tennis.court_booking.domain.valueobject.TimeSlot;

import java.util.List;

/**
 * Domain service that orchestrates booking reservation logic.
 * Coordinates business policies and creates valid bookings.
 * ID assignment is delegated to the persistence layer.
 */
public class BookingDomainService {
    private final OpeningHoursPolicy openingHoursPolicy;
    private final OverlappingReservationsPolicy overlappingReservationsPolicy;

    /**
     * Creates a new BookingDomainService with the specified policies.
     *
     * @param openingHoursPolicy             policy for validating opening hours
     * @param overlappingReservationsPolicy  policy for validating overlapping reservations
     * @throws IllegalArgumentException if any policy is null
     */
    public BookingDomainService(
            OpeningHoursPolicy openingHoursPolicy,
            OverlappingReservationsPolicy overlappingReservationsPolicy) {
        if (openingHoursPolicy == null) {
            throw new IllegalArgumentException("OpeningHoursPolicy cannot be null");
        }
        if (overlappingReservationsPolicy == null) {
            throw new IllegalArgumentException("OverlappingReservationsPolicy cannot be null");
        }

        this.openingHoursPolicy = openingHoursPolicy;
        this.overlappingReservationsPolicy = overlappingReservationsPolicy;
    }

    /**
     * Attempts to reserve a booking for the given time slot.
     * Validates the time slot against business policies.
     * Returns a Booking without an ID - ID will be assigned by the persistence layer.
     *
     * @param timeSlot         the time slot to reserve
     * @param existingBookings the list of existing bookings to check for conflicts
     * @return a new Booking with null ID if validation passes
     * @throws BusinessException if any business rule is violated
     */
    public Booking reserve(TimeSlot timeSlot, List<Booking> existingBookings) {
        // Validate against opening hours policy (also validates timeSlot != null)
        openingHoursPolicy.validate(timeSlot);

        // Validate against overlapping reservations policy (also validates existingBookings != null)
        overlappingReservationsPolicy.validate(timeSlot, existingBookings);

        // If all validations pass, create and return the booking without ID
        // ID will be assigned by the persistence layer upon save
        return new Booking(null, timeSlot);
    }
}
