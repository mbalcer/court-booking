package com.tennis.court_booking.adapter.out.persistence.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingJpaEntityTest {

    @Test
    void shouldCreateBookingJpaEntityWithId() {
        // Given
        Long id = 1L;
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        // When
        BookingJpaEntity entity = new BookingJpaEntity(id, date, startTime, endTime);

        // Then
        assertEquals(id, entity.getId());
        assertEquals(date, entity.getDate());
        assertEquals(startTime, entity.getStartTime());
        assertEquals(endTime, entity.getEndTime());
    }

    @Test
    void shouldCreateBookingJpaEntityWithoutId() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        // When
        BookingJpaEntity entity = new BookingJpaEntity(date, startTime, endTime);

        // Then
        assertNull(entity.getId());
        assertEquals(date, entity.getDate());
        assertEquals(startTime, entity.getStartTime());
        assertEquals(endTime, entity.getEndTime());
    }

    @Test
    void shouldCreateBookingJpaEntityUsingNoArgsConstructor() {
        // When
        BookingJpaEntity entity = new BookingJpaEntity();

        // Then
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getDate());
        assertNull(entity.getStartTime());
        assertNull(entity.getEndTime());
    }

    @Test
    void shouldBeEqualWhenSameId() {
        // Given
        Long id = 1L;
        LocalDate date1 = LocalDate.of(2024, 1, 15);
        LocalTime start1 = LocalTime.of(10, 0);
        LocalTime end1 = LocalTime.of(11, 0);

        LocalDate date2 = LocalDate.of(2024, 1, 16);
        LocalTime start2 = LocalTime.of(14, 0);
        LocalTime end2 = LocalTime.of(15, 0);

        BookingJpaEntity entity1 = new BookingJpaEntity(id, date1, start1, end1);
        BookingJpaEntity entity2 = new BookingJpaEntity(id, date2, start2, end2);

        // When & Then
        assertEquals(entity1, entity2);
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        BookingJpaEntity entity1 = new BookingJpaEntity(1L, date, start, end);
        BookingJpaEntity entity2 = new BookingJpaEntity(2L, date, start, end);

        // When & Then
        assertNotEquals(entity1, entity2);
        assertNotEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenOneIdIsNull() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        BookingJpaEntity entity1 = new BookingJpaEntity(1L, date, start, end);
        BookingJpaEntity entity2 = new BookingJpaEntity(date, start, end);

        // When & Then
        assertNotEquals(entity1, entity2);
    }

    @Test
    void shouldNotBeEqualWhenBothIdsAreNull() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        BookingJpaEntity entity1 = new BookingJpaEntity(date, start, end);
        BookingJpaEntity entity2 = new BookingJpaEntity(date, start, end);

        // When & Then
        // Two entities with null IDs are not equal (different instances)
        assertNotEquals(entity1, entity2);
    }

    @Test
    void shouldBeEqualToItself() {
        // Given
        BookingJpaEntity entity = new BookingJpaEntity(
                1L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        // When & Then
        assertEquals(entity, entity);
    }

    @Test
    void shouldNotBeEqualToNull() {
        // Given
        BookingJpaEntity entity = new BookingJpaEntity(
                1L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        // When & Then
        assertNotEquals(entity, null);
    }

    @Test
    void shouldNotBeEqualToDifferentClass() {
        // Given
        BookingJpaEntity entity = new BookingJpaEntity(
                1L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        // When & Then
        assertNotEquals(entity, "not a booking entity");
    }

    @Test
    void shouldHaveConsistentHashCode() {
        // Given
        BookingJpaEntity entity = new BookingJpaEntity(
                1L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        // When
        int hashCode1 = entity.hashCode();
        int hashCode2 = entity.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void shouldHaveToStringWithAllFields() {
        // Given
        BookingJpaEntity entity = new BookingJpaEntity(
                1L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        // When
        String toString = entity.toString();

        // Then
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("date=2024-01-15"));
        assertTrue(toString.contains("startTime=10:00"));
        assertTrue(toString.contains("endTime=11:00"));
    }
}
