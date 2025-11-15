package com.tennis.court_booking.domain.policy;

import com.tennis.court_booking.domain.entity.Booking;
import com.tennis.court_booking.domain.exception.BusinessException;
import com.tennis.court_booking.domain.valueobject.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OverlappingReservationsPolicy Tests")
class OverlappingReservationsPolicyTest {

    private OverlappingReservationsPolicy policy;
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 1, 15);

    @BeforeEach
    void setUp() {
        policy = new OverlappingReservationsPolicy();
    }

    @Test
    @DisplayName("Should validate when there are no existing bookings")
    void shouldValidateWhenNoExistingBookings() {
        TimeSlot timeSlot = new TimeSlot(
            TEST_DATE,
            LocalTime.of(10, 0),
            LocalTime.of(11, 0)
        );
        List<Booking> existingBookings = new ArrayList<>();

        assertDoesNotThrow(() -> policy.validate(timeSlot, existingBookings));
    }

    @Test
    @DisplayName("Should validate when new booking does not overlap with existing bookings")
    void shouldValidateWhenNoOverlap() {
        TimeSlot newTimeSlot = new TimeSlot(
            TEST_DATE,
            LocalTime.of(10, 0),
            LocalTime.of(11, 0)
        );

        Booking existingBooking1 = new Booking(
            1L,
            new TimeSlot(TEST_DATE, LocalTime.of(8, 0), LocalTime.of(9, 0))
        );
        Booking existingBooking2 = new Booking(
            2L,
            new TimeSlot(TEST_DATE, LocalTime.of(12, 0), LocalTime.of(13, 0))
        );

        List<Booking> existingBookings = Arrays.asList(existingBooking1, existingBooking2);

        assertDoesNotThrow(() -> policy.validate(newTimeSlot, existingBookings));
    }

    @Test
    @DisplayName("Should validate when bookings are on different dates")
    void shouldValidateWhenDifferentDates() {
        TimeSlot newTimeSlot = new TimeSlot(
            TEST_DATE,
            LocalTime.of(10, 0),
            LocalTime.of(11, 0)
        );

        Booking existingBooking = new Booking(
            1L,
            new TimeSlot(TEST_DATE.plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0))
        );

        List<Booking> existingBookings = Arrays.asList(existingBooking);

        assertDoesNotThrow(() -> policy.validate(newTimeSlot, existingBookings));
    }

    @Test
    @DisplayName("Should validate when new booking ends exactly when existing booking starts")
    void shouldValidateWhenNewEndsWhenExistingStarts() {
        TimeSlot newTimeSlot = new TimeSlot(
            TEST_DATE,
            LocalTime.of(9, 0),
            LocalTime.of(10, 0)
        );

        Booking existingBooking = new Booking(
            1L,
            new TimeSlot(TEST_DATE, LocalTime.of(10, 0), LocalTime.of(11, 0))
        );

        List<Booking> existingBookings = Arrays.asList(existingBooking);

        assertDoesNotThrow(() -> policy.validate(newTimeSlot, existingBookings));
    }

    @Test
    @DisplayName("Should validate when new booking starts exactly when existing booking ends")
    void shouldValidateWhenNewStartsWhenExistingEnds() {
        TimeSlot newTimeSlot = new TimeSlot(
            TEST_DATE,
            LocalTime.of(11, 0),
            LocalTime.of(12, 0)
        );

        Booking existingBooking = new Booking(
            1L,
            new TimeSlot(TEST_DATE, LocalTime.of(10, 0), LocalTime.of(11, 0))
        );

        List<Booking> existingBookings = Arrays.asList(existingBooking);

        assertDoesNotThrow(() -> policy.validate(newTimeSlot, existingBookings));
    }

    @Test
    @DisplayName("Should throw BusinessException when bookings overlap completely")
    void shouldThrowExceptionWhenCompleteOverlap() {
        TimeSlot newTimeSlot = new TimeSlot(
            TEST_DATE,
            LocalTime.of(10, 0),
            LocalTime.of(11, 0)
        );

        Booking existingBooking = new Booking(
            1L,
            new TimeSlot(TEST_DATE, LocalTime.of(10, 0), LocalTime.of(11, 0))
        );

        List<Booking> existingBookings = Arrays.asList(existingBooking);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> policy.validate(newTimeSlot, existingBookings)
        );
        assertTrue(exception.getMessage().contains("overlaps with an existing booking"));
        assertTrue(exception.getMessage().contains("Existing booking ID: 1"));
    }

    @Test
    @DisplayName("Should throw BusinessException when new booking starts during existing booking")
    void shouldThrowExceptionWhenNewStartsDuringExisting() {
        TimeSlot newTimeSlot = new TimeSlot(
            TEST_DATE,
            LocalTime.of(10, 30),
            LocalTime.of(11, 30)
        );

        Booking existingBooking = new Booking(
            1L,
            new TimeSlot(TEST_DATE, LocalTime.of(10, 0), LocalTime.of(11, 0))
        );

        List<Booking> existingBookings = Arrays.asList(existingBooking);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> policy.validate(newTimeSlot, existingBookings)
        );
        assertTrue(exception.getMessage().contains("overlaps with an existing booking"));
    }

    @Test
    @DisplayName("Should throw BusinessException when new booking ends during existing booking")
    void shouldThrowExceptionWhenNewEndsDuringExisting() {
        TimeSlot newTimeSlot = new TimeSlot(
            TEST_DATE,
            LocalTime.of(9, 30),
            LocalTime.of(10, 30)
        );

        Booking existingBooking = new Booking(
            1L,
            new TimeSlot(TEST_DATE, LocalTime.of(10, 0), LocalTime.of(11, 0))
        );

        List<Booking> existingBookings = Arrays.asList(existingBooking);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> policy.validate(newTimeSlot, existingBookings)
        );
        assertTrue(exception.getMessage().contains("overlaps with an existing booking"));
    }

    @Test
    @DisplayName("Should throw BusinessException when new booking completely contains existing booking")
    void shouldThrowExceptionWhenNewContainsExisting() {
        TimeSlot newTimeSlot = new TimeSlot(
            TEST_DATE,
            LocalTime.of(9, 0),
            LocalTime.of(12, 0)
        );

        Booking existingBooking = new Booking(
            1L,
            new TimeSlot(TEST_DATE, LocalTime.of(10, 0), LocalTime.of(11, 0))
        );

        List<Booking> existingBookings = Arrays.asList(existingBooking);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> policy.validate(newTimeSlot, existingBookings)
        );
        assertTrue(exception.getMessage().contains("overlaps with an existing booking"));
    }

    @Test
    @DisplayName("Should throw BusinessException when existing booking completely contains new booking")
    void shouldThrowExceptionWhenExistingContainsNew() {
        TimeSlot newTimeSlot = new TimeSlot(
            TEST_DATE,
            LocalTime.of(10, 15),
            LocalTime.of(10, 45)
        );

        Booking existingBooking = new Booking(
            1L,
            new TimeSlot(TEST_DATE, LocalTime.of(10, 0), LocalTime.of(11, 0))
        );

        List<Booking> existingBookings = Arrays.asList(existingBooking);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> policy.validate(newTimeSlot, existingBookings)
        );
        assertTrue(exception.getMessage().contains("overlaps with an existing booking"));
    }

    @Test
    @DisplayName("Should throw BusinessException on first overlap when multiple bookings exist")
    void shouldThrowExceptionOnFirstOverlapWithMultipleBookings() {
        TimeSlot newTimeSlot = new TimeSlot(
            TEST_DATE,
            LocalTime.of(10, 30),
            LocalTime.of(11, 30)
        );

        Booking booking1 = new Booking(
            1L,
            new TimeSlot(TEST_DATE, LocalTime.of(8, 0), LocalTime.of(9, 0))
        );
        Booking booking2 = new Booking(
            2L,
            new TimeSlot(TEST_DATE, LocalTime.of(10, 0), LocalTime.of(11, 0))
        );
        Booking booking3 = new Booking(
            3L,
            new TimeSlot(TEST_DATE, LocalTime.of(12, 0), LocalTime.of(13, 0))
        );

        List<Booking> existingBookings = Arrays.asList(booking1, booking2, booking3);

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> policy.validate(newTimeSlot, existingBookings)
        );
        assertTrue(exception.getMessage().contains("Existing booking ID: 2"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when time slot is null")
    void shouldThrowExceptionWhenTimeSlotIsNull() {
        List<Booking> existingBookings = new ArrayList<>();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> policy.validate(null, existingBookings)
        );
        assertEquals("TimeSlot cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when existing bookings list is null")
    void shouldThrowExceptionWhenExistingBookingsIsNull() {
        TimeSlot timeSlot = new TimeSlot(
            TEST_DATE,
            LocalTime.of(10, 0),
            LocalTime.of(11, 0)
        );

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> policy.validate(timeSlot, null)
        );
        assertEquals("Existing bookings list cannot be null", exception.getMessage());
    }
}
