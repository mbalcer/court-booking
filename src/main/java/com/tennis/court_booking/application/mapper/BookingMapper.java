package com.tennis.court_booking.application.mapper;

import com.tennis.court_booking.application.port.in.BookingResponse;
import com.tennis.court_booking.domain.entity.Booking;
import com.tennis.court_booking.domain.event.BookingCreatedEvent;

/**
 * Mapper for converting Booking domain entity to DTOs and events.
 * This mapper is part of the application layer and handles the translation
 * between domain objects and application-layer DTOs/events.
 *
 * Responsibilities:
 * - Convert Booking entity to BookingResponse DTO (for API responses)
 * - Convert Booking entity to BookingCreatedEvent (for event publishing)
 *
 * This prevents domain entities from leaking to the adapter layer and
 * maintains clean boundaries in the hexagonal architecture.
 */
public class BookingMapper {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private BookingMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Converts a Booking domain entity to a BookingResponse DTO.
     * This DTO is used by inbound adapters (e.g., REST controllers) to return
     * booking information to clients.
     *
     * @param booking the booking entity to convert
     * @return a BookingResponse DTO containing the booking data
     * @throws IllegalArgumentException if booking is null
     */
    public static BookingResponse toBookingResponse(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }

        return new BookingResponse(
                booking.getId(),
                booking.getTimeSlot().getDate(),
                booking.getTimeSlot().getStart(),
                booking.getTimeSlot().getEnd()
        );
    }

    /**
     * Converts a Booking domain entity to a BookingCreatedEvent.
     * This event is published to external systems (e.g., message queues)
     * to notify them of the new booking.
     *
     * @param booking the booking entity to convert
     * @return a BookingCreatedEvent containing the booking data
     * @throws IllegalArgumentException if booking is null
     */
    public static BookingCreatedEvent toBookingCreatedEvent(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }

        return new BookingCreatedEvent(
                booking.getId(),
                booking.getTimeSlot().getDate(),
                booking.getTimeSlot().getStart(),
                booking.getTimeSlot().getEnd()
        );
    }
}
