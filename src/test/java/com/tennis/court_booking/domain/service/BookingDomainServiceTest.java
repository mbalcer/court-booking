package com.tennis.court_booking.domain.service;

import com.tennis.court_booking.domain.entity.Booking;
import com.tennis.court_booking.domain.exception.BusinessException;
import com.tennis.court_booking.domain.policy.OpeningHoursPolicy;
import com.tennis.court_booking.domain.policy.OverlappingReservationsPolicy;
import com.tennis.court_booking.domain.valueobject.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingDomainServiceTest {

    private BookingDomainService bookingDomainService;
    private OpeningHoursPolicy openingHoursPolicy;
    private OverlappingReservationsPolicy overlappingReservationsPolicy;

    @BeforeEach
    void setUp() {
        // Configure standard opening hours: 08:00 - 22:00
        openingHoursPolicy = new OpeningHoursPolicy(
                LocalTime.of(8, 0),
                LocalTime.of(22, 0)
        );
        overlappingReservationsPolicy = new OverlappingReservationsPolicy();

        bookingDomainService = new BookingDomainService(
                openingHoursPolicy,
                overlappingReservationsPolicy
        );
    }

    @Test
    @DisplayName("Should create service with valid policies")
    void shouldCreateServiceWithValidPolicies() {
        assertNotNull(bookingDomainService);
    }

    @Test
    @DisplayName("Should throw exception when OpeningHoursPolicy is null")
    void shouldThrowExceptionWhenOpeningHoursPolicyIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BookingDomainService(null, overlappingReservationsPolicy)
        );
        assertEquals("OpeningHoursPolicy cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when OverlappingReservationsPolicy is null")
    void shouldThrowExceptionWhenOverlappingReservationsPolicyIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BookingDomainService(openingHoursPolicy, null)
        );
        assertEquals("OverlappingReservationsPolicy cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should successfully reserve a valid time slot with no existing bookings")
    void shouldSuccessfullyReserveValidTimeSlot() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot timeSlot = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        List<Booking> existingBookings = new ArrayList<>();

        // When
        Booking booking = bookingDomainService.reserve(timeSlot, existingBookings);

        // Then
        assertNotNull(booking);
        assertNotNull(booking.getId());
        assertEquals(timeSlot, booking.getTimeSlot());
    }

    @Test
    @DisplayName("Should successfully reserve multiple non-overlapping time slots")
    void shouldSuccessfullyReserveMultipleNonOverlappingTimeSlots() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        List<Booking> existingBookings = new ArrayList<>();

        // When - First booking
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        Booking booking1 = bookingDomainService.reserve(slot1, existingBookings);
        existingBookings.add(booking1);

        // When - Second booking (non-overlapping)
        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(11, 0), LocalTime.of(12, 0));
        Booking booking2 = bookingDomainService.reserve(slot2, existingBookings);
        existingBookings.add(booking2);

        // When - Third booking (non-overlapping)
        TimeSlot slot3 = new TimeSlot(date, LocalTime.of(14, 0), LocalTime.of(15, 0));
        Booking booking3 = bookingDomainService.reserve(slot3, existingBookings);

        // Then
        assertNotNull(booking1);
        assertNotNull(booking2);
        assertNotNull(booking3);
        assertEquals(3, existingBookings.size() + 1); // +1 for booking3 not yet added
        assertNotEquals(booking1.getId(), booking2.getId());
        assertNotEquals(booking2.getId(), booking3.getId());
    }

    @Test
    @DisplayName("Should throw exception when TimeSlot is null")
    void shouldThrowExceptionWhenTimeSlotIsNull() {
        // Given
        List<Booking> existingBookings = new ArrayList<>();

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingDomainService.reserve(null, existingBookings)
        );
        assertEquals("TimeSlot cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when existing bookings list is null")
    void shouldThrowExceptionWhenExistingBookingsListIsNull() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot timeSlot = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingDomainService.reserve(timeSlot, null)
        );
        assertEquals("Existing bookings list cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw BusinessException when booking starts before opening time")
    void shouldThrowExceptionWhenBookingStartsBeforeOpeningTime() {
        // Given - Booking at 07:00-08:00 (before 08:00 opening)
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot timeSlot = new TimeSlot(date, LocalTime.of(7, 0), LocalTime.of(8, 0));
        List<Booking> existingBookings = new ArrayList<>();

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookingDomainService.reserve(timeSlot, existingBookings)
        );
        assertTrue(exception.getMessage().contains("Booking cannot start before opening time"));
    }

    @Test
    @DisplayName("Should throw BusinessException when booking ends after closing time")
    void shouldThrowExceptionWhenBookingEndsAfterClosingTime() {
        // Given - Booking at 21:30-22:30 (ends after 22:00 closing)
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot timeSlot = new TimeSlot(date, LocalTime.of(21, 30), LocalTime.of(22, 30));
        List<Booking> existingBookings = new ArrayList<>();

        // When & Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookingDomainService.reserve(timeSlot, existingBookings)
        );
        assertTrue(exception.getMessage().contains("Booking cannot end after closing time"));
    }

    @Test
    @DisplayName("Should throw BusinessException when time slot overlaps with existing booking")
    void shouldThrowExceptionWhenTimeSlotOverlapsWithExistingBooking() {
        // Given - Existing booking at 10:00-11:00
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot existingSlot = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        Booking existingBooking = new Booking(1L, existingSlot);
        List<Booking> existingBookings = List.of(existingBooking);

        // When - Try to book overlapping slot 10:30-11:30
        TimeSlot overlappingSlot = new TimeSlot(date, LocalTime.of(10, 30), LocalTime.of(11, 30));

        // Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookingDomainService.reserve(overlappingSlot, existingBookings)
        );
        assertTrue(exception.getMessage().contains("overlaps with an existing booking"));
    }

    @Test
    @DisplayName("Should throw BusinessException when time slot completely contains existing booking")
    void shouldThrowExceptionWhenTimeSlotCompletelyContainsExistingBooking() {
        // Given - Existing booking at 10:30-11:00
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot existingSlot = new TimeSlot(date, LocalTime.of(10, 30), LocalTime.of(11, 0));
        Booking existingBooking = new Booking(1L, existingSlot);
        List<Booking> existingBookings = List.of(existingBooking);

        // When - Try to book larger overlapping slot 10:00-12:00
        TimeSlot overlappingSlot = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(12, 0));

        // Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookingDomainService.reserve(overlappingSlot, existingBookings)
        );
        assertTrue(exception.getMessage().contains("overlaps with an existing booking"));
    }

    @Test
    @DisplayName("Should successfully reserve when time slot is on different date")
    void shouldSuccessfullyReserveWhenTimeSlotIsOnDifferentDate() {
        // Given - Existing booking on 2024-01-15
        LocalDate date1 = LocalDate.of(2024, 1, 15);
        TimeSlot existingSlot = new TimeSlot(date1, LocalTime.of(10, 0), LocalTime.of(11, 0));
        Booking existingBooking = new Booking(1L, existingSlot);
        List<Booking> existingBookings = List.of(existingBooking);

        // When - Book same time on different date 2024-01-16
        LocalDate date2 = LocalDate.of(2024, 1, 16);
        TimeSlot newSlot = new TimeSlot(date2, LocalTime.of(10, 0), LocalTime.of(11, 0));
        Booking booking = bookingDomainService.reserve(newSlot, existingBookings);

        // Then
        assertNotNull(booking);
        assertEquals(newSlot, booking.getTimeSlot());
    }

    @Test
    @DisplayName("Should successfully reserve when time slot touches but does not overlap existing booking")
    void shouldSuccessfullyReserveWhenTimeSlotTouchesButDoesNotOverlap() {
        // Given - Existing booking at 10:00-11:00
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot existingSlot = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        Booking existingBooking = new Booking(1L, existingSlot);
        List<Booking> existingBookings = List.of(existingBooking);

        // When - Book adjacent slot 11:00-12:00 (touches but doesn't overlap)
        TimeSlot adjacentSlot = new TimeSlot(date, LocalTime.of(11, 0), LocalTime.of(12, 0));
        Booking booking = bookingDomainService.reserve(adjacentSlot, existingBookings);

        // Then
        assertNotNull(booking);
        assertEquals(adjacentSlot, booking.getTimeSlot());
    }

    @Test
    @DisplayName("Should successfully reserve at boundary times (opening and closing)")
    void shouldSuccessfullyReserveAtBoundaryTimes() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        List<Booking> existingBookings = new ArrayList<>();

        // When - Book at opening time 08:00-09:00
        TimeSlot morningSlot = new TimeSlot(date, LocalTime.of(8, 0), LocalTime.of(9, 0));
        Booking morningBooking = bookingDomainService.reserve(morningSlot, existingBookings);

        // When - Book at closing boundary 21:00-22:00
        TimeSlot eveningSlot = new TimeSlot(date, LocalTime.of(21, 0), LocalTime.of(22, 0));
        Booking eveningBooking = bookingDomainService.reserve(eveningSlot, List.of(morningBooking));

        // Then
        assertNotNull(morningBooking);
        assertNotNull(eveningBooking);
    }

    @Test
    @DisplayName("Should handle complex scenario with multiple existing bookings")
    void shouldHandleComplexScenarioWithMultipleExistingBookings() {
        // Given - Multiple existing bookings throughout the day
        LocalDate date = LocalDate.of(2024, 1, 15);
        List<Booking> existingBookings = new ArrayList<>();
        existingBookings.add(new Booking(1L, new TimeSlot(date, LocalTime.of(9, 0), LocalTime.of(10, 0))));
        existingBookings.add(new Booking(2L, new TimeSlot(date, LocalTime.of(11, 0), LocalTime.of(12, 0))));
        existingBookings.add(new Booking(3L, new TimeSlot(date, LocalTime.of(14, 0), LocalTime.of(15, 30))));

        // When - Try to book a free slot between existing bookings
        TimeSlot freeSlot = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        Booking booking = bookingDomainService.reserve(freeSlot, existingBookings);

        // Then
        assertNotNull(booking);
        assertEquals(freeSlot, booking.getTimeSlot());
    }

    @Test
    @DisplayName("Should fail when trying to book slot that partially overlaps multiple bookings")
    void shouldFailWhenTryingToBookSlotThatPartiallyOverlapsMultipleBookings() {
        // Given - Two separate bookings
        LocalDate date = LocalDate.of(2024, 1, 15);
        List<Booking> existingBookings = new ArrayList<>();
        existingBookings.add(new Booking(1L, new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0))));
        existingBookings.add(new Booking(2L, new TimeSlot(date, LocalTime.of(14, 0), LocalTime.of(15, 0))));

        // When - Try to book slot that overlaps with first booking
        TimeSlot overlappingSlot = new TimeSlot(date, LocalTime.of(10, 30), LocalTime.of(13, 0));

        // Then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> bookingDomainService.reserve(overlappingSlot, existingBookings)
        );
        assertTrue(exception.getMessage().contains("overlaps with an existing booking"));
    }

    @Test
    @DisplayName("Should generate unique IDs for each booking")
    void shouldGenerateUniqueIDsForEachBooking() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        List<Booking> existingBookings = new ArrayList<>();

        // When - Create multiple bookings
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(9, 0), LocalTime.of(10, 0));
        Booking booking1 = bookingDomainService.reserve(slot1, existingBookings);

        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        Booking booking2 = bookingDomainService.reserve(slot2, List.of(booking1));

        TimeSlot slot3 = new TimeSlot(date, LocalTime.of(11, 0), LocalTime.of(12, 0));
        Booking booking3 = bookingDomainService.reserve(slot3, List.of(booking1, booking2));

        // Then
        assertNotNull(booking1.getId());
        assertNotNull(booking2.getId());
        assertNotNull(booking3.getId());
        assertNotEquals(booking1.getId(), booking2.getId());
        assertNotEquals(booking2.getId(), booking3.getId());
        assertNotEquals(booking1.getId(), booking3.getId());
    }
}
