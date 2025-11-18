package com.tennis.court_booking.adapter.out.persistence;

import com.tennis.court_booking.application.port.out.BookingRepository;
import com.tennis.court_booking.domain.entity.Booking;
import com.tennis.court_booking.domain.valueobject.TimeSlot;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * In-memory implementation of the BookingRepository port.
 * This is a temporary stub adapter for development and testing purposes.
 *
 * NOTE: This implementation will be replaced with a JPA-based repository
 * adapter in Step 9 (Persistence Adapter).
 *
 * Thread-safe implementation using ConcurrentHashMap for storage.
 */
public class InMemoryBookingRepository implements BookingRepository {

    private final Map<Long, Booking> bookings = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Finds all bookings for a specific date.
     *
     * @param date the date to search for
     * @return list of bookings on the specified date (empty list if none found)
     */
    @Override
    public List<Booking> findByDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        return bookings.values().stream()
                .filter(booking -> booking.getTimeSlot().getDate().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * Saves a booking to the in-memory repository.
     * If the booking has no ID (null), a new booking is created and assigned an ID.
     * If the booking has an ID, it updates the existing booking.
     *
     * @param booking the booking to save
     * @return the saved booking with assigned ID
     */
    @Override
    public Booking save(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }

        // If booking has no ID, generate one and create new booking with ID
        if (booking.getId() == null) {
            Long newId = idGenerator.getAndIncrement();
            Booking bookingWithId = new Booking(newId, booking.getTimeSlot());
            bookings.put(newId, bookingWithId);
            return bookingWithId;
        } else {
            // Update existing booking
            bookings.put(booking.getId(), booking);
            return booking;
        }
    }

    /**
     * Finds a booking by its unique identifier.
     *
     * @param id the booking ID
     * @return an Optional containing the booking if found, empty otherwise
     */
    @Override
    public Optional<Booking> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        return Optional.ofNullable(bookings.get(id));
    }

    /**
     * Deletes a booking by its unique identifier.
     *
     * @param id the booking ID to delete
     */
    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        bookings.remove(id);
    }

    /**
     * Clears all bookings from the repository.
     * Useful for testing purposes.
     */
    public void clear() {
        bookings.clear();
        idGenerator.set(1);
    }

    /**
     * Returns the total number of bookings in the repository.
     * Useful for testing purposes.
     *
     * @return the number of bookings
     */
    public int count() {
        return bookings.size();
    }
}
