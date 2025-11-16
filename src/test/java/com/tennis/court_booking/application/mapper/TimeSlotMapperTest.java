package com.tennis.court_booking.application.mapper;

import com.tennis.court_booking.application.port.in.ReserveCommand;
import com.tennis.court_booking.domain.exception.InvalidTimeSlotException;
import com.tennis.court_booking.domain.valueobject.TimeSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeSlotMapperTest {

    // ========== Constructor Tests ==========

    @Test
    @DisplayName("Should not be able to instantiate TimeSlotMapper")
    void shouldNotBeAbleToInstantiateTimeSlotMapper() throws NoSuchMethodException {
        Constructor<TimeSlotMapper> constructor = TimeSlotMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        InvocationTargetException exception = assertThrows(
                InvocationTargetException.class,
                constructor::newInstance
        );

        assertTrue(exception.getCause() instanceof UnsupportedOperationException);
        assertEquals("This is a utility class and cannot be instantiated", exception.getCause().getMessage());
    }

    // ========== Successful Mapping Tests ==========

    @Test
    @DisplayName("Should map ReserveCommand to TimeSlot successfully")
    void shouldMapReserveCommandToTimeSlotSuccessfully() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);
        ReserveCommand command = new ReserveCommand(date, start, end);

        // When
        TimeSlot timeSlot = TimeSlotMapper.toTimeSlot(command);

        // Then
        assertNotNull(timeSlot);
        assertEquals(date, timeSlot.getDate());
        assertEquals(start, timeSlot.getStart());
        assertEquals(end, timeSlot.getEnd());
    }

    @Test
    @DisplayName("Should map ReserveCommand with different times")
    void shouldMapReserveCommandWithDifferentTimes() {
        // Given
        LocalDate date = LocalDate.of(2024, 6, 20);
        LocalTime start = LocalTime.of(14, 30);
        LocalTime end = LocalTime.of(16, 0);
        ReserveCommand command = new ReserveCommand(date, start, end);

        // When
        TimeSlot timeSlot = TimeSlotMapper.toTimeSlot(command);

        // Then
        assertNotNull(timeSlot);
        assertEquals(date, timeSlot.getDate());
        assertEquals(start, timeSlot.getStart());
        assertEquals(end, timeSlot.getEnd());
    }

    @Test
    @DisplayName("Should map ReserveCommand for early morning slot")
    void shouldMapReserveCommandForEarlyMorningSlot() {
        // Given
        LocalDate date = LocalDate.of(2024, 12, 25);
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(9, 0);
        ReserveCommand command = new ReserveCommand(date, start, end);

        // When
        TimeSlot timeSlot = TimeSlotMapper.toTimeSlot(command);

        // Then
        assertNotNull(timeSlot);
        assertEquals(date, timeSlot.getDate());
        assertEquals(start, timeSlot.getStart());
        assertEquals(end, timeSlot.getEnd());
    }

    @Test
    @DisplayName("Should map ReserveCommand for late evening slot")
    void shouldMapReserveCommandForLateEveningSlot() {
        // Given
        LocalDate date = LocalDate.of(2024, 3, 10);
        LocalTime start = LocalTime.of(21, 0);
        LocalTime end = LocalTime.of(22, 0);
        ReserveCommand command = new ReserveCommand(date, start, end);

        // When
        TimeSlot timeSlot = TimeSlotMapper.toTimeSlot(command);

        // Then
        assertNotNull(timeSlot);
        assertEquals(date, timeSlot.getDate());
        assertEquals(start, timeSlot.getStart());
        assertEquals(end, timeSlot.getEnd());
    }

    // ========== Null Parameter Tests ==========

    @Test
    @DisplayName("Should throw exception when ReserveCommand is null")
    void shouldThrowExceptionWhenReserveCommandIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> TimeSlotMapper.toTimeSlot(null)
        );

        assertEquals("ReserveCommand cannot be null", exception.getMessage());
    }

    // ========== Invalid Data Tests ==========

    @Test
    @DisplayName("Should propagate InvalidTimeSlotException when end time is before start time")
    void shouldPropagateInvalidTimeSlotExceptionWhenEndTimeIsBeforeStartTime() {
        // Given - invalid command (end before start)
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(15, 0);
        LocalTime end = LocalTime.of(14, 0);
        ReserveCommand command = new ReserveCommand(date, start, end);

        // When & Then
        InvalidTimeSlotException exception = assertThrows(
                InvalidTimeSlotException.class,
                () -> TimeSlotMapper.toTimeSlot(command)
        );

        assertTrue(exception.getMessage().contains("End time must be after start time"));
    }

    @Test
    @DisplayName("Should propagate InvalidTimeSlotException when start equals end")
    void shouldPropagateInvalidTimeSlotExceptionWhenStartEqualsEnd() {
        // Given - invalid command (start equals end)
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime time = LocalTime.of(10, 0);
        ReserveCommand command = new ReserveCommand(date, time, time);

        // When & Then
        InvalidTimeSlotException exception = assertThrows(
                InvalidTimeSlotException.class,
                () -> TimeSlotMapper.toTimeSlot(command)
        );

        assertTrue(exception.getMessage().contains("End time must be after start time"));
    }

    // ========== Multiple Mappings Tests ==========

    @Test
    @DisplayName("Should create independent TimeSlot instances for different commands")
    void shouldCreateIndependentTimeSlotInstancesForDifferentCommands() {
        // Given
        ReserveCommand command1 = new ReserveCommand(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );
        ReserveCommand command2 = new ReserveCommand(
                LocalDate.of(2024, 1, 16),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0)
        );

        // When
        TimeSlot timeSlot1 = TimeSlotMapper.toTimeSlot(command1);
        TimeSlot timeSlot2 = TimeSlotMapper.toTimeSlot(command2);

        // Then
        assertNotNull(timeSlot1);
        assertNotNull(timeSlot2);
        assertNotEquals(timeSlot1, timeSlot2);
        assertEquals(LocalDate.of(2024, 1, 15), timeSlot1.getDate());
        assertEquals(LocalDate.of(2024, 1, 16), timeSlot2.getDate());
    }

    @Test
    @DisplayName("Should create equal TimeSlot instances for identical commands")
    void shouldCreateEqualTimeSlotInstancesForIdenticalCommands() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        ReserveCommand command1 = new ReserveCommand(date, start, end);
        ReserveCommand command2 = new ReserveCommand(date, start, end);

        // When
        TimeSlot timeSlot1 = TimeSlotMapper.toTimeSlot(command1);
        TimeSlot timeSlot2 = TimeSlotMapper.toTimeSlot(command2);

        // Then
        assertNotNull(timeSlot1);
        assertNotNull(timeSlot2);
        assertEquals(timeSlot1, timeSlot2);
    }
}
