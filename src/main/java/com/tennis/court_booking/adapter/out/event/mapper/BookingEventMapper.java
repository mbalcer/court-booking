package com.tennis.court_booking.adapter.out.event.mapper;

import com.tennis.court_booking.adapter.out.event.dto.BookingCreatedKafkaEvent;
import com.tennis.court_booking.domain.event.BookingCreatedEvent;

/**
 * Mapper for converting domain events to Kafka-specific event DTOs.
 * This mapper keeps the adapter layer separate from the domain layer,
 * allowing the Kafka message format to evolve independently of domain events.
 *
 * Following the same pattern as other mappers in the application:
 * - Static utility class with private constructor
 * - Input validation with null checks
 * - Throws IllegalArgumentException for invalid inputs
 * - No business logic, only structural conversion
 */
public class BookingEventMapper {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private BookingEventMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Converts a domain BookingCreatedEvent to a Kafka-specific BookingCreatedKafkaEvent.
     * This method performs a simple structural conversion without any business logic.
     *
     * @param domainEvent the domain event to convert
     * @return a Kafka-specific event DTO
     * @throws IllegalArgumentException if domainEvent is null
     */
    public static BookingCreatedKafkaEvent toKafkaEvent(BookingCreatedEvent domainEvent) {
        if (domainEvent == null) {
            throw new IllegalArgumentException("Domain event cannot be null");
        }

        return new BookingCreatedKafkaEvent(
                domainEvent.getBookingId(),
                domainEvent.getDate(),
                domainEvent.getStartTime(),
                domainEvent.getEndTime()
        );
    }
}
