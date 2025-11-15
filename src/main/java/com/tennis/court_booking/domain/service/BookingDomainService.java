package com.tennis.court_booking.domain.service;

import com.tennis.court_booking.domain.entity.Booking;
import com.tennis.court_booking.domain.exception.BusinessException;
import com.tennis.court_booking.domain.policy.OpeningHoursPolicy;
import com.tennis.court_booking.domain.policy.OverlappingReservationsPolicy;
import com.tennis.court_booking.domain.valueobject.TimeSlot;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Domain service that orchestrates booking reservation logic.
 * Coordinates business policies and creates valid bookings.
 */
public class BookingDomainService {
    private final OpeningHoursPolicy openingHoursPolicy;
    private final OverlappingReservationsPolicy overlappingReservationsPolicy;
    private final AtomicLong idGenerator;

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
        this.idGenerator = new AtomicLong(1);
    }

    /**
     * Attempts to reserve a booking for the given time slot.
     * Validates the time slot against business policies.
     *
     * @param timeSlot         the time slot to reserve
     * @param existingBookings the list of existing bookings to check for conflicts
     * @return a new Booking if validation passes
     * @throws BusinessException        if any business rule is violated
     * @throws IllegalArgumentException if timeSlot or existingBookings is null
     */
    public Booking reserve(TimeSlot timeSlot, List<Booking> existingBookings) {
        if (timeSlot == null) {
            throw new IllegalArgumentException("TimeSlot cannot be null");
        }
        if (existingBookings == null) {
            throw new IllegalArgumentException("Existing bookings list cannot be null");
        }

        // Validate against opening hours policy
        openingHoursPolicy.validate(timeSlot);

        // Validate against overlapping reservations policy
        overlappingReservationsPolicy.validate(timeSlot, existingBookings);

        // If all validations pass, create and return the booking
        return new Booking(idGenerator.getAndIncrement(), timeSlot);
    }
}
