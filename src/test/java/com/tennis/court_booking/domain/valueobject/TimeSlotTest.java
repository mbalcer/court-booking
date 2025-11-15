package com.tennis.court_booking.domain.valueobject;

import com.tennis.court_booking.domain.exception.InvalidTimeSlotException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeSlotTest {

    @Test
    void shouldCreateValidTimeSlot() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        TimeSlot timeSlot = new TimeSlot(date, start, end);

        assertNotNull(timeSlot);
        assertEquals(date, timeSlot.getDate());
        assertEquals(start, timeSlot.getStart());
        assertEquals(end, timeSlot.getEnd());
    }

    @Test
    void shouldThrowExceptionWhenDateIsNull() {
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        InvalidTimeSlotException exception = assertThrows(
                InvalidTimeSlotException.class,
                () -> new TimeSlot(null, start, end)
        );
        assertEquals("Date cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenStartTimeIsNull() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime end = LocalTime.of(11, 0);

        InvalidTimeSlotException exception = assertThrows(
                InvalidTimeSlotException.class,
                () -> new TimeSlot(date, null, end)
        );
        assertEquals("Start time cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsNull() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(10, 0);

        InvalidTimeSlotException exception = assertThrows(
                InvalidTimeSlotException.class,
                () -> new TimeSlot(date, start, null)
        );
        assertEquals("End time cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEndTimeIsBeforeStartTime() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(11, 0);
        LocalTime end = LocalTime.of(10, 0);

        InvalidTimeSlotException exception = assertThrows(
                InvalidTimeSlotException.class,
                () -> new TimeSlot(date, start, end)
        );
        assertEquals("End time must be after start time", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEndTimeEqualsStartTime() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(10, 0);

        InvalidTimeSlotException exception = assertThrows(
                InvalidTimeSlotException.class,
                () -> new TimeSlot(date, start, end)
        );
        assertEquals("End time must be after start time", exception.getMessage());
    }

    @Test
    void shouldDetectOverlappingTimeSlots() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(10, 30), LocalTime.of(11, 30));

        assertTrue(slot1.overlaps(slot2));
        assertTrue(slot2.overlaps(slot1));
    }

    @Test
    void shouldDetectNonOverlappingTimeSlots() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(11, 0), LocalTime.of(12, 0));

        assertFalse(slot1.overlaps(slot2));
        assertFalse(slot2.overlaps(slot1));
    }

    @Test
    void shouldDetectOverlapWhenOneSlotsContainsAnother() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot outerSlot = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(12, 0));
        TimeSlot innerSlot = new TimeSlot(date, LocalTime.of(10, 30), LocalTime.of(11, 30));

        assertTrue(outerSlot.overlaps(innerSlot));
        assertTrue(innerSlot.overlaps(outerSlot));
    }

    @Test
    void shouldNotOverlapWhenOnDifferentDates() {
        TimeSlot slot1 = new TimeSlot(LocalDate.of(2024, 1, 15), LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimeSlot slot2 = new TimeSlot(LocalDate.of(2024, 1, 16), LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertFalse(slot1.overlaps(slot2));
        assertFalse(slot2.overlaps(slot1));
    }

    @Test
    void shouldNotOverlapWithNull() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot slot = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertFalse(slot.overlaps(null));
    }

    @Test
    void shouldDetectOverlapAtStartBoundary() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(9, 30), LocalTime.of(10, 30));

        assertTrue(slot1.overlaps(slot2));
        assertTrue(slot2.overlaps(slot1));
    }

    @Test
    void shouldDetectOverlapAtEndBoundary() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(10, 59), LocalTime.of(12, 0));

        assertTrue(slot1.overlaps(slot2));
        assertTrue(slot2.overlaps(slot1));
    }

    @Test
    void shouldOverlapWithIdenticalTimeSlot() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        TimeSlot slot1 = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));
        TimeSlot slot2 = new TimeSlot(date, LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertTrue(slot1.overlaps(slot2));
        assertTrue(slot2.overlaps(slot1));
    }
}
