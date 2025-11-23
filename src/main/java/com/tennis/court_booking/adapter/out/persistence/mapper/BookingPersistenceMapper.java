package com.tennis.court_booking.adapter.out.persistence.mapper;

import com.tennis.court_booking.adapter.out.persistence.entity.BookingJpaEntity;
import com.tennis.court_booking.domain.entity.Booking;
import com.tennis.court_booking.domain.valueobject.TimeSlot;

/**
 * Mapper for converting between domain Booking entities and JPA BookingJpaEntity.
 * This mapper maintains the separation between domain and persistence layers
 * in the hexagonal architecture.
 *
 * Static utility class with no state.
 */
public class BookingPersistenceMapper {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private BookingPersistenceMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Converts a domain Booking entity to a JPA entity for persistence.
     *
     * @param booking the domain booking entity
     * @return the JPA booking entity
     * @throws IllegalArgumentException if booking is null
     */
    public static BookingJpaEntity toJpaEntity(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }

        TimeSlot timeSlot = booking.getTimeSlot();

        if (booking.getId() == null) {
            // New booking without ID - let JPA generate the ID
            return new BookingJpaEntity(
                    timeSlot.getDate(),
                    timeSlot.getStart(),
                    timeSlot.getEnd()
            );
        } else {
            // Existing booking with ID
            return new BookingJpaEntity(
                    booking.getId(),
                    timeSlot.getDate(),
                    timeSlot.getStart(),
                    timeSlot.getEnd()
            );
        }
    }

    /**
     * Converts a JPA entity to a domain Booking entity.
     *
     * @param jpaEntity the JPA booking entity
     * @return the domain booking entity
     * @throws IllegalArgumentException if jpaEntity is null
     */
    public static Booking toDomainEntity(BookingJpaEntity jpaEntity) {
        if (jpaEntity == null) {
            throw new IllegalArgumentException("JPA entity cannot be null");
        }

        TimeSlot timeSlot = new TimeSlot(
                jpaEntity.getDate(),
                jpaEntity.getStartTime(),
                jpaEntity.getEndTime()
        );

        return new Booking(jpaEntity.getId(), timeSlot);
    }
}
