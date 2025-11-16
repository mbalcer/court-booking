package com.tennis.court_booking.application.port.out;

import com.tennis.court_booking.domain.entity.Booking;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Outbound port (repository interface) for booking persistence operations.
 * This interface defines the contract for persisting and retrieving bookings.
 *
 * In hexagonal architecture, this is a secondary/driven port that will be
 * implemented by an outbound adapter (e.g., JPA repository adapter).
 * The domain and application layers depend on this interface, not on the implementation.
 */
public interface BookingRepository {

    /**
     * Finds all bookings for a specific date.
     * Used to check for overlapping reservations.
     *
     * @param date the date to search for
     * @return list of bookings on the specified date (empty list if none found)
     */
    List<Booking> findByDate(LocalDate date);

    /**
     * Saves a booking to the repository.
     * If the booking has no ID (null), a new booking is created and assigned an ID.
     * If the booking has an ID, it updates the existing booking.
     *
     * @param booking the booking to save
     * @return the saved booking with assigned ID
     */
    Booking save(Booking booking);

    /**
     * Finds a booking by its unique identifier.
     *
     * @param id the booking ID
     * @return an Optional containing the booking if found, empty otherwise
     */
    Optional<Booking> findById(Long id);

    /**
     * Deletes a booking by its unique identifier.
     *
     * @param id the booking ID to delete
     */
    void delete(Long id);
}
