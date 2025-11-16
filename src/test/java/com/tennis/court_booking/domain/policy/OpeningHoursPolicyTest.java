package com.tennis.court_booking.domain.policy;

import com.tennis.court_booking.domain.exception.BusinessException;
import com.tennis.court_booking.domain.valueobject.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpeningHoursPolicy Tests")
class OpeningHoursPolicyTest {

    private OpeningHoursPolicy policy;
    private static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    private static final LocalTime CLOSING_TIME = LocalTime.of(20, 0);
    private static final LocalDate TEST_DATE = LocalDate.of(2024, 1, 15);

    @BeforeEach
    void setUp() {
        policy = new OpeningHoursPolicy(OPENING_TIME, CLOSING_TIME);
    }

    @Test
    @DisplayName("Should create policy with valid opening and closing times")
    void shouldCreatePolicyWithValidTimes() {
        OpeningHoursPolicy policy = new OpeningHoursPolicy(
            LocalTime.of(8, 0),
            LocalTime.of(20, 0)
        );

        assertNotNull(policy);
        assertEquals(LocalTime.of(8, 0), policy.getOpeningTime());
        assertEquals(LocalTime.of(20, 0), policy.getClosingTime());
    }

    @Test
    @DisplayName("Should throw exception when opening time is null")
    void shouldThrowExceptionWhenOpeningTimeIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new OpeningHoursPolicy(null, LocalTime.of(20, 0))
        );
        assertEquals("Opening time cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when closing time is null")
    void shouldThrowExceptionWhenClosingTimeIsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new OpeningHoursPolicy(LocalTime.of(8, 0), null)
        );
        assertEquals("Closing time cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when closing time is not after opening time")
    void shouldThrowExceptionWhenClosingTimeIsNotAfterOpeningTime() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new OpeningHoursPolicy(LocalTime.of(20, 0), LocalTime.of(8, 0))
        );
        assertEquals("Closing time must be after opening time", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when closing time equals opening time")
    void shouldThrowExceptionWhenClosingTimeEqualsOpeningTime() {
        LocalTime sameTime = LocalTime.of(10, 0);
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new OpeningHoursPolicy(sameTime, sameTime)
        );
        assertEquals("Closing time must be after opening time", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate time slot within opening hours")
    void shouldValidateTimeSlotWithinOpeningHours() {
        TimeSlot timeSlot = new TimeSlot(
            TEST_DATE,
            LocalTime.of(10, 0),
            LocalTime.of(11, 0)
        );

        assertDoesNotThrow(() -> policy.validate(timeSlot));
    }

    @Test
    @DisplayName("Should validate time slot that starts at opening time")
    void shouldValidateTimeSlotStartingAtOpeningTime() {
        TimeSlot timeSlot = new TimeSlot(
            TEST_DATE,
            OPENING_TIME,
            LocalTime.of(9, 0)
        );

        assertDoesNotThrow(() -> policy.validate(timeSlot));
    }

    @Test
    @DisplayName("Should validate time slot that ends at closing time")
    void shouldValidateTimeSlotEndingAtClosingTime() {
        TimeSlot timeSlot = new TimeSlot(
            TEST_DATE,
            LocalTime.of(19, 0),
            CLOSING_TIME
        );

        assertDoesNotThrow(() -> policy.validate(timeSlot));
    }

    @Test
    @DisplayName("Should validate time slot that spans entire opening hours")
    void shouldValidateTimeSlotSpanningEntireOpeningHours() {
        TimeSlot timeSlot = new TimeSlot(
            TEST_DATE,
            OPENING_TIME,
            CLOSING_TIME
        );

        assertDoesNotThrow(() -> policy.validate(timeSlot));
    }

    @Test
    @DisplayName("Should throw BusinessException when time slot starts before opening time")
    void shouldThrowExceptionWhenStartsBeforeOpeningTime() {
        TimeSlot timeSlot = new TimeSlot(
            TEST_DATE,
            LocalTime.of(7, 0),
            LocalTime.of(9, 0)
        );

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> policy.validate(timeSlot)
        );
        assertTrue(exception.getMessage().contains("cannot start before opening time"));
    }

    @Test
    @DisplayName("Should throw BusinessException when time slot ends after closing time")
    void shouldThrowExceptionWhenEndsAfterClosingTime() {
        TimeSlot timeSlot = new TimeSlot(
            TEST_DATE,
            LocalTime.of(19, 0),
            LocalTime.of(21, 0)
        );

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> policy.validate(timeSlot)
        );
        assertTrue(exception.getMessage().contains("cannot end after closing time"));
    }

    @Test
    @DisplayName("Should throw BusinessException when time slot is completely outside opening hours")
    void shouldThrowExceptionWhenCompletelyOutsideOpeningHours() {
        TimeSlot timeSlot = new TimeSlot(
            TEST_DATE,
            LocalTime.of(21, 0),
            LocalTime.of(22, 0)
        );

        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> policy.validate(timeSlot)
        );
        assertTrue(exception.getMessage().contains("cannot end after closing time"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when validating null time slot")
    void shouldThrowExceptionWhenValidatingNullTimeSlot() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> policy.validate(null)
        );
        assertEquals("TimeSlot cannot be null", exception.getMessage());
    }
}
