package com.tennis.court_booking.application.mapper;

import com.tennis.court_booking.application.port.in.BookingResponse;
import com.tennis.court_booking.domain.entity.Booking;
import com.tennis.court_booking.domain.event.BookingCreatedEvent;
import com.tennis.court_booking.domain.valueobject.TimeSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("Should not be able to instantiate BookingMapper")
    void shouldNotBeAbleToInstantiateBookingMapper() throws NoSuchMethodException {
        Constructor<BookingMapper> constructor = BookingMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(
                InvocationTargetException.class,
                constructor::newInstance
        );

        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
        assertEquals("This is a utility class and cannot be instantiated", exception.getCause().getMessage());
    }

    // ========== toBookingResponse Tests ==========

    @Test
    @DisplayName("Should map Booking to BookingResponse successfully")
    void shouldMapBookingToBookingResponseSuccessfully() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);
        TimeSlot timeSlot = new TimeSlot(date, start, end);
        Booking booking = new Booking(1L, timeSlot);

        // When
        BookingResponse response = BookingMapper.toBookingResponse(booking);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(date, response.getDate());
        assertEquals(start, response.getStartTime());
        assertEquals(end, response.getEndTime());
    }

    @Test
    @DisplayName("Should map Booking with different ID to BookingResponse")
    void shouldMapBookingWithDifferentIdToBookingResponse() {
        // Given
        LocalDate date = LocalDate.of(2024, 6, 20);
        LocalTime start = LocalTime.of(14, 30);
        LocalTime end = LocalTime.of(16, 0);
        TimeSlot timeSlot = new TimeSlot(date, start, end);
        Booking booking = new Booking(999L, timeSlot);

        // When
        BookingResponse response = BookingMapper.toBookingResponse(booking);

        // Then
        assertNotNull(response);
        assertEquals(999L, response.getId());
        assertEquals(date, response.getDate());
        assertEquals(start, response.getStartTime());
        assertEquals(end, response.getEndTime());
    }

    @Test
    @DisplayName("Should throw exception when Booking is null for toBookingResponse")
    void shouldThrowExceptionWhenBookingIsNullForToBookingResponse() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BookingMapper.toBookingResponse(null)
        );

        assertEquals("Booking cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should map multiple bookings to responses independently")
    void shouldMapMultipleBookingsToResponsesIndependently() {
        // Given
        TimeSlot timeSlot1 = new TimeSlot(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );
        TimeSlot timeSlot2 = new TimeSlot(
                LocalDate.of(2024, 1, 16),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0)
        );
        Booking booking1 = new Booking(1L, timeSlot1);
        Booking booking2 = new Booking(2L, timeSlot2);

        // When
        BookingResponse response1 = BookingMapper.toBookingResponse(booking1);
        BookingResponse response2 = BookingMapper.toBookingResponse(booking2);

        // Then
        assertNotEquals(response1, response2);
        assertEquals(1L, response1.getId());
        assertEquals(2L, response2.getId());
    }

    // ========== toBookingCreatedEvent Tests ==========

    @Test
    @DisplayName("Should map Booking to BookingCreatedEvent successfully")
    void shouldMapBookingToBookingCreatedEventSuccessfully() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);
        TimeSlot timeSlot = new TimeSlot(date, start, end);
        Booking booking = new Booking(1L, timeSlot);

        // When
        BookingCreatedEvent event = BookingMapper.toBookingCreatedEvent(booking);

        // Then
        assertNotNull(event);
        assertEquals(1L, event.getBookingId());
        assertEquals(date, event.getDate());
        assertEquals(start, event.getStartTime());
        assertEquals(end, event.getEndTime());
    }

    @Test
    @DisplayName("Should map Booking with different ID to BookingCreatedEvent")
    void shouldMapBookingWithDifferentIdToBookingCreatedEvent() {
        // Given
        LocalDate date = LocalDate.of(2024, 6, 20);
        LocalTime start = LocalTime.of(14, 30);
        LocalTime end = LocalTime.of(16, 0);
        TimeSlot timeSlot = new TimeSlot(date, start, end);
        Booking booking = new Booking(999L, timeSlot);

        // When
        BookingCreatedEvent event = BookingMapper.toBookingCreatedEvent(booking);

        // Then
        assertNotNull(event);
        assertEquals(999L, event.getBookingId());
        assertEquals(date, event.getDate());
        assertEquals(start, event.getStartTime());
        assertEquals(end, event.getEndTime());
    }

    @Test
    @DisplayName("Should throw exception when Booking is null for toBookingCreatedEvent")
    void shouldThrowExceptionWhenBookingIsNullForToBookingCreatedEvent() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BookingMapper.toBookingCreatedEvent(null)
        );

        assertEquals("Booking cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should map multiple bookings to events independently")
    void shouldMapMultipleBookingsToEventsIndependently() {
        // Given
        TimeSlot timeSlot1 = new TimeSlot(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );
        TimeSlot timeSlot2 = new TimeSlot(
                LocalDate.of(2024, 1, 16),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0)
        );
        Booking booking1 = new Booking(1L, timeSlot1);
        Booking booking2 = new Booking(2L, timeSlot2);

        // When
        BookingCreatedEvent event1 = BookingMapper.toBookingCreatedEvent(booking1);
        BookingCreatedEvent event2 = BookingMapper.toBookingCreatedEvent(booking2);

        // Then
        assertNotEquals(event1, event2);
        assertEquals(1L, event1.getBookingId());
        assertEquals(2L, event2.getBookingId());
    }

    // ========== Consistency Tests ==========

    @Test
    @DisplayName("Should produce consistent data when mapping same booking to response and event")
    void shouldProduceConsistentDataWhenMappingSameBookingToResponseAndEvent() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);
        TimeSlot timeSlot = new TimeSlot(date, start, end);
        Booking booking = new Booking(42L, timeSlot);

        // When
        BookingResponse response = BookingMapper.toBookingResponse(booking);
        BookingCreatedEvent event = BookingMapper.toBookingCreatedEvent(booking);

        // Then
        assertEquals(response.getId(), event.getBookingId());
        assertEquals(response.getDate(), event.getDate());
        assertEquals(response.getStartTime(), event.getStartTime());
        assertEquals(response.getEndTime(), event.getEndTime());
    }

    @Test
    @DisplayName("Should map booking with early morning time slot")
    void shouldMapBookingWithEarlyMorningTimeSlot() {
        // Given
        LocalDate date = LocalDate.of(2024, 12, 25);
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(9, 0);
        TimeSlot timeSlot = new TimeSlot(date, start, end);
        Booking booking = new Booking(5L, timeSlot);

        // When
        BookingResponse response = BookingMapper.toBookingResponse(booking);
        BookingCreatedEvent event = BookingMapper.toBookingCreatedEvent(booking);

        // Then
        assertEquals(start, response.getStartTime());
        assertEquals(end, response.getEndTime());
        assertEquals(start, event.getStartTime());
        assertEquals(end, event.getEndTime());
    }

    @Test
    @DisplayName("Should map booking with late evening time slot")
    void shouldMapBookingWithLateEveningTimeSlot() {
        // Given
        LocalDate date = LocalDate.of(2024, 3, 10);
        LocalTime start = LocalTime.of(21, 0);
        LocalTime end = LocalTime.of(22, 0);
        TimeSlot timeSlot = new TimeSlot(date, start, end);
        Booking booking = new Booking(7L, timeSlot);

        // When
        BookingResponse response = BookingMapper.toBookingResponse(booking);
        BookingCreatedEvent event = BookingMapper.toBookingCreatedEvent(booking);

        // Then
        assertEquals(start, response.getStartTime());
        assertEquals(end, response.getEndTime());
        assertEquals(start, event.getStartTime());
        assertEquals(end, event.getEndTime());
    }
}
