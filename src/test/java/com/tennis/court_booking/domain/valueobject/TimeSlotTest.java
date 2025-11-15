package com.tennis.court_booking.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeSlotTest {

    @Test
    void shouldCreateValidTimeSlot() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        // when
        TimeSlot timeSlot = new TimeSlot(date, start, end);

        // then
        assertNotNull(timeSlot);
        assertEquals(date, timeSlot.getDate());
        assertEquals(start, timeSlot.getStart());
        assertEquals(end, timeSlot.getEnd());
    }

    @Test
    void shouldThrowExceptionWhenDateIsNull() {
        // given
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new TimeSlot(null, start, end)
        );
        assertEquals("Date cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenStartTimeIsNull() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime end = LocalTime.of(11, 0);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new TimeSlot(date, null, end)
        );
        assertEquals("Start time cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsNull() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(10, 0);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new TimeSlot(date, start, null)
        );
        assertEquals("End time cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsBeforeStartTime() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(11, 0);
        LocalTime end = LocalTime.of(10, 0);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new TimeSlot(date, start, end)
        );
        assertEquals("End time must be after start time", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEndTimeEqualsStartTime() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(10, 0);

        // when & then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new TimeSlot(date, start, end)
        );
        assertEquals("End time must be after start time", exception.getMessage());
    }

    @Test
    void shouldDetectOverlappingTimeSlots() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(10, 30), LocalTime.of(11, 30));

        // when & then
        assertTrue(slot1.overlaps(slot2));
        assertTrue(slot2.overlaps(slot1));
    }

    @Test
    void shouldDetectNonOverlappingTimeSlots() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(11, 0), LocalTime.of(12, 0));

        // when & then
        assertFalse(slot1.overlaps(slot2));
        assertFalse(slot2.overlaps(slot1));
    }

    @Test
    void shouldDetectOverlapWhenOneSlotsContainsAnother() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot outerSlot = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(12, 0));
        TimeSlot innerSlot = new TimeSlot(date, LocalTime.of(10, 30), LocalTime.of(11, 30));

        // when & then
        assertTrue(outerSlot.overlaps(innerSlot));
        assertTrue(innerSlot.overlaps(outerSlot));
    }

    @Test
    void shouldNotOverlapWhenOnDifferentDates() {
        // given
        TimeSlot slot1 = new TimeSlot(LocalDate.of(2024, 1, 15), LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimeSlot slot2 = new TimeSlot(LocalDate.of(2024, 1, 16), LocalTime.of(10, 0), LocalTime.of(11, 0));

        // when & then
        assertFalse(slot1.overlaps(slot2));
        assertFalse(slot2.overlaps(slot1));
    }

    @Test
    void shouldNotOverlapWithNull() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot slot = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));

        // when & then
        assertFalse(slot.overlaps(null));
    }

    @Test
    void shouldDetectOverlapAtStartBoundary() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(9, 30), LocalTime.of(10, 30));

        // when & then
        assertTrue(slot1.overlaps(slot2));
        assertTrue(slot2.overlaps(slot1));
    }

    @Test
    void shouldDetectOverlapAtEndBoundary() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(10, 59), LocalTime.of(12, 0));

        // when & then
        assertTrue(slot1.overlaps(slot2));
        assertTrue(slot2.overlaps(slot1));
    }

    @Test
    void shouldOverlapWithIdenticalTimeSlot() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));

        // when & then
        assertTrue(slot1.overlaps(slot2));
        assertTrue(slot2.overlaps(slot1));
    }
}
