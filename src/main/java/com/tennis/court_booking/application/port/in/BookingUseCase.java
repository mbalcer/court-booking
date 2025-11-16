package com.tennis.court_booking.application.port.in;

/**
 * Inbound port (use case interface) for booking operations.
 * This interface defines the application's core use cases from the perspective
 * of the driving actors (e.g., REST controllers, CLI, etc.).
 *
 * In hexagonal architecture, this is a primary/driving port that will be
 * implemented by an application service and called by inbound adapters.
 */
public interface BookingUseCase {

    /**
     * Reserves a new court booking for the specified time slot.
     *
     * This use case:
     * 1. Validates the time slot (converts command to domain TimeSlot)
     * 2. Validates business rules (opening hours, overlapping reservations)
     * 3. Persists the booking to the repository
     * 4. Publishes a booking created event
     *
     * @param command the reservation command containing date, start, and end times
     * @return the booking response DTO with assigned ID and booking details
     * @throws com.tennis.court_booking.domain.exception.InvalidTimeSlotException if the time slot is invalid
     * @throws com.tennis.court_booking.domain.exception.BusinessException if business rules are violated
     */
    BookingResponse reserve(ReserveCommand command);
}
