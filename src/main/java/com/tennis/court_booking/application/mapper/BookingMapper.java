package com.tennis.court_booking.application.mapper;

import com.tennis.court_booking.application.port.in.BookingResponse;
import com.tennis.court_booking.domain.entity.Booking;
import com.tennis.court_booking.domain.event.BookingCreatedEvent;

/**
 * Mapper for converting Booking entity to DTOs and events.
 */
public class BookingMapper {

    private BookingMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Converts a Booking to BookingResponse DTO.
     *
     * @param booking the booking entity
     * @return a BookingResponse DTO
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
     * Converts a Booking to BookingCreatedEvent.
     *
     * @param booking the booking entity
     * @return a BookingCreatedEvent
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
