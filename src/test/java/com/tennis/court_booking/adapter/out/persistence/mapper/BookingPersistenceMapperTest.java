package com.tennis.court_booking.adapter.out.persistence.mapper;

import com.tennis.court_booking.adapter.out.persistence.entity.BookingJpaEntity;
import com.tennis.court_booking.domain.entity.Booking;
import com.tennis.court_booking.domain.exception.InvalidTimeSlotException;
import com.tennis.court_booking.domain.valueobject.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingPersistenceMapperTest {

    @Test
    void shouldNotBeInstantiable() {
        // When & Then
        assertThrows(UnsupportedOperationException.class, BookingPersistenceMapper::new);
    }

    @Test
    void shouldConvertDomainBookingWithIdToJpaEntity() {
        // Given
        Long id = 1L;
        TimeSlot timeSlot = new TimeSlot(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );
        Booking domainBooking = new Booking(id, timeSlot);

        // When
        BookingJpaEntity jpaEntity = BookingPersistenceMapper.toJpaEntity(domainBooking);

        // Then
        assertNotNull(jpaEntity);
        assertEquals(id, jpaEntity.getId());
        assertEquals(timeSlot.getDate(), jpaEntity.getDate());
        assertEquals(timeSlot.getStart(), jpaEntity.getStartTime());
        assertEquals(timeSlot.getEnd(), jpaEntity.getEndTime());
    }

    @Test
    void shouldConvertDomainBookingWithoutIdToJpaEntity() {
        // Given
        TimeSlot timeSlot = new TimeSlot(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );
        Booking domainBooking = new Booking(null, timeSlot);

        // When
        BookingJpaEntity jpaEntity = BookingPersistenceMapper.toJpaEntity(domainBooking);

        // Then
        assertNotNull(jpaEntity);
        assertNull(jpaEntity.getId());
        assertEquals(timeSlot.getDate(), jpaEntity.getDate());
        assertEquals(timeSlot.getStart(), jpaEntity.getStartTime());
        assertEquals(timeSlot.getEnd(), jpaEntity.getEndTime());
    }

    @Test
    void shouldThrowExceptionWhenConvertingNullDomainBookingToJpaEntity() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BookingPersistenceMapper.toJpaEntity(null)
        );

        assertEquals("Booking cannot be null", exception.getMessage());
    }

    @Test
    void shouldConvertJpaEntityToDomainBooking() {
        // Given
        Long id = 1L;
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        BookingJpaEntity jpaEntity = new BookingJpaEntity(id, date, startTime, endTime);

        // When
        Booking domainBooking = BookingPersistenceMapper.toDomainEntity(jpaEntity);

        // Then
        assertNotNull(domainBooking);
        assertEquals(id, domainBooking.getId());
        assertEquals(date, domainBooking.getTimeSlot().getDate());
        assertEquals(startTime, domainBooking.getTimeSlot().getStart());
        assertEquals(endTime, domainBooking.getTimeSlot().getEnd());
    }

    @Test
    void shouldConvertJpaEntityWithoutIdToDomainBooking() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        BookingJpaEntity jpaEntity = new BookingJpaEntity(null, date, startTime, endTime);

        // When
        Booking domainBooking = BookingPersistenceMapper.toDomainEntity(jpaEntity);

        // Then
        assertNotNull(domainBooking);
        assertNull(domainBooking.getId());
        assertEquals(date, domainBooking.getTimeSlot().getDate());
        assertEquals(startTime, domainBooking.getTimeSlot().getStart());
        assertEquals(endTime, domainBooking.getTimeSlot().getEnd());
    }

    @Test
    void shouldThrowExceptionWhenConvertingNullJpaEntityToDomainBooking() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BookingPersistenceMapper.toDomainEntity(null)
        );

        assertEquals("JPA entity cannot be null", exception.getMessage());
    }

    @Test
    void shouldPropagateInvalidTimeSlotExceptionWhenConvertingJpaEntityWithInvalidData() {
        // Given - end time before start time
        BookingJpaEntity jpaEntity = new BookingJpaEntity(
                1L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(11, 0),
                LocalTime.of(10, 0)
        );

        // When & Then
        assertThrows(
                InvalidTimeSlotException.class,
                () -> BookingPersistenceMapper.toDomainEntity(jpaEntity)
        );
    }

    @Test
    void shouldPropagateInvalidTimeSlotExceptionWhenJpaEntityHasNullDate() {
        // Given
        BookingJpaEntity jpaEntity = new BookingJpaEntity(
                1L,
                null,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        // When & Then
        assertThrows(
                InvalidTimeSlotException.class,
                () -> BookingPersistenceMapper.toDomainEntity(jpaEntity)
        );
    }

    @Test
    void shouldPropagateInvalidTimeSlotExceptionWhenJpaEntityHasNullStartTime() {
        // Given
        BookingJpaEntity jpaEntity = new BookingJpaEntity(
                1L,
                LocalDate.of(2024, 1, 15),
                null,
                LocalTime.of(11, 0)
        );

        // When & Then
        assertThrows(
                InvalidTimeSlotException.class,
                () -> BookingPersistenceMapper.toDomainEntity(jpaEntity)
        );
    }

    @Test
    void shouldPropagateInvalidTimeSlotExceptionWhenJpaEntityHasNullEndTime() {
        // Given
        BookingJpaEntity jpaEntity = new BookingJpaEntity(
                1L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                null
        );

        // When & Then
        assertThrows(
                InvalidTimeSlotException.class,
                () -> BookingPersistenceMapper.toDomainEntity(jpaEntity)
        );
    }

    @Test
    void shouldMaintainConsistencyInRoundTripConversion() {
        // Given
        Long id = 1L;
        TimeSlot timeSlot = new TimeSlot(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );
        Booking originalBooking = new Booking(id, timeSlot);

        // When - convert to JPA entity and back to domain
        BookingJpaEntity jpaEntity = BookingPersistenceMapper.toJpaEntity(originalBooking);
        Booking convertedBooking = BookingPersistenceMapper.toDomainEntity(jpaEntity);

        // Then
        assertEquals(originalBooking.getId(), convertedBooking.getId());
        assertEquals(originalBooking.getTimeSlot(), convertedBooking.getTimeSlot());
    }

    @Test
    void shouldMaintainConsistencyInRoundTripConversionWithoutId() {
        // Given
        TimeSlot timeSlot = new TimeSlot(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );
        Booking originalBooking = new Booking(null, timeSlot);

        // When - convert to JPA entity and back to domain
        BookingJpaEntity jpaEntity = BookingPersistenceMapper.toJpaEntity(originalBooking);
        Booking convertedBooking = BookingPersistenceMapper.toDomainEntity(jpaEntity);

        // Then
        assertNull(convertedBooking.getId());
        assertEquals(originalBooking.getTimeSlot(), convertedBooking.getTimeSlot());
    }

    @Test
    void shouldHandleDifferentTimeValues() {
        // Given
        TimeSlot timeSlot = new TimeSlot(
                LocalDate.of(2025, 12, 31),
                LocalTime.of(23, 30),
                LocalTime.of(23, 59)
        );
        Booking domainBooking = new Booking(999L, timeSlot);

        // When
        BookingJpaEntity jpaEntity = BookingPersistenceMapper.toJpaEntity(domainBooking);
        Booking convertedBooking = BookingPersistenceMapper.toDomainEntity(jpaEntity);

        // Then
        assertEquals(domainBooking.getId(), convertedBooking.getId());
        assertEquals(domainBooking.getTimeSlot(), convertedBooking.getTimeSlot());
    }
}
