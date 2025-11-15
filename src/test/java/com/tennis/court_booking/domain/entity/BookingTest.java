package com.tennis.court_booking.domain.entity;

import com.tennis.court_booking.domain.valueobject.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

    @Test
    void shouldCreateValidBooking() {
        Long id = 1L;
        TimeSlot timeSlot = new TimeSlot(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        Booking booking = new Booking(id, timeSlot);

        assertNotNull(booking);
        assertEquals(id, booking.getId());
        assertEquals(timeSlot, booking.getTimeSlot());
    }

    @Test
    void shouldCreateBookingWithNullId() {
        TimeSlot timeSlot = new TimeSlot(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        Booking booking = new Booking(null, timeSlot);

        assertNotNull(booking);
        assertNull(booking.getId());
        assertEquals(timeSlot, booking.getTimeSlot());
    }

    @Test
    void shouldThrowExceptionWhenTimeSlotIsNull() {
        Long id = 1L;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Booking(id, null)
        );
        assertEquals("TimeSlot cannot be null", exception.getMessage());
    }

    @Test
    void shouldBeEqualWhenIdsAreEqual() {
        Long id = 1L;
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

        Booking booking1 = new Booking(id, timeSlot1);
        Booking booking2 = new Booking(id, timeSlot2);

        assertEquals(booking1, booking2);
        assertEquals(booking1.hashCode(), booking2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenIdsAreDifferent() {
        TimeSlot timeSlot = new TimeSlot(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        Booking booking1 = new Booking(1L, timeSlot);
        Booking booking2 = new Booking(2L, timeSlot);

        assertNotEquals(booking1, booking2);
    }

    @Test
    void shouldNotBeEqualToNull() {
        Long id = 1L;
        TimeSlot timeSlot = new TimeSlot(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        Booking booking = new Booking(id, timeSlot);

        assertNotEquals(booking, null);
    }

    @Test
    void shouldNotBeEqualToDifferentClass() {
        Long id = 1L;
        TimeSlot timeSlot = new TimeSlot(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        Booking booking = new Booking(id, timeSlot);
        String notABooking = "Not a booking";

        assertNotEquals(booking, notABooking);
    }

    @Test
    void shouldHaveStringRepresentation() {
        Long id = 1L;
        TimeSlot timeSlot = new TimeSlot(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        Booking booking = new Booking(id, timeSlot);
        String stringRepresentation = booking.toString();

        assertNotNull(stringRepresentation);
        assertTrue(stringRepresentation.contains("Booking"));
        assertTrue(stringRepresentation.contains("id=" + id));
    }

    @Test
    void shouldBeReflexive() {
        Long id = 1L;
        TimeSlot timeSlot = new TimeSlot(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        Booking booking = new Booking(id, timeSlot);

        assertEquals(booking, booking);
    }

    @Test
    void shouldBeSymmetric() {
        Long id = 1L;
        TimeSlot timeSlot = new TimeSlot(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        Booking booking1 = new Booking(id, timeSlot);
        Booking booking2 = new Booking(id, timeSlot);

        assertEquals(booking1, booking2);
        assertEquals(booking2, booking1);
    }

    @Test
    void shouldBeTransitive() {
        Long id = 1L;
        TimeSlot timeSlot = new TimeSlot(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        Booking booking1 = new Booking(id, timeSlot);
        Booking booking2 = new Booking(id, timeSlot);
        Booking booking3 = new Booking(id, timeSlot);

        assertEquals(booking1, booking2);
        assertEquals(booking2, booking3);
        assertEquals(booking1, booking3);
    }
}
