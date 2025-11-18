package com.tennis.court_booking.adapter.out.event.mapper;

import com.tennis.court_booking.adapter.out.event.dto.BookingCreatedKafkaEvent;
import com.tennis.court_booking.domain.event.BookingCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BookingEventMapper.
 * Tests the conversion from domain events to Kafka-specific event DTOs.
 *
 * Test coverage:
 * - Successful mapping scenarios
 * - Null parameter handling
 * - Data consistency and accuracy
 * - Utility class instantiation prevention
 */
@DisplayName("BookingEventMapper Tests")
class BookingEventMapperTest {

    @Test
    @DisplayName("Should not allow instantiation of utility class")
    void shouldNotAllowInstantiation() {
        assertThrows(UnsupportedOperationException.class, () -> {
            // Use reflection to access private constructor
            var constructor = BookingEventMapper.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    @DisplayName("Should successfully convert domain event to Kafka event")
    void shouldConvertDomainEventToKafkaEvent() {
        // Given
        BookingCreatedEvent domainEvent = new BookingCreatedEvent(
                1L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        // When
        BookingCreatedKafkaEvent kafkaEvent = BookingEventMapper.toKafkaEvent(domainEvent);

        // Then
        assertNotNull(kafkaEvent);
        assertEquals(domainEvent.getBookingId(), kafkaEvent.getBookingId());
        assertEquals(domainEvent.getDate(), kafkaEvent.getDate());
        assertEquals(domainEvent.getStartTime(), kafkaEvent.getStartTime());
        assertEquals(domainEvent.getEndTime(), kafkaEvent.getEndTime());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when domain event is null")
    void shouldThrowExceptionWhenDomainEventIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BookingEventMapper.toKafkaEvent(null)
        );

        assertEquals("Domain event cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should preserve booking ID during conversion")
    void shouldPreserveBookingIdDuringConversion() {
        // Given
        Long expectedBookingId = 42L;
        BookingCreatedEvent domainEvent = new BookingCreatedEvent(
                expectedBookingId,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        // When
        BookingCreatedKafkaEvent kafkaEvent = BookingEventMapper.toKafkaEvent(domainEvent);

        // Then
        assertEquals(expectedBookingId, kafkaEvent.getBookingId());
    }

    @Test
    @DisplayName("Should preserve date during conversion")
    void shouldPreserveDateDuringConversion() {
        // Given
        LocalDate expectedDate = LocalDate.of(2024, 12, 31);
        BookingCreatedEvent domainEvent = new BookingCreatedEvent(
                1L,
                expectedDate,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        // When
        BookingCreatedKafkaEvent kafkaEvent = BookingEventMapper.toKafkaEvent(domainEvent);

        // Then
        assertEquals(expectedDate, kafkaEvent.getDate());
    }

    @Test
    @DisplayName("Should preserve start time during conversion")
    void shouldPreserveStartTimeDuringConversion() {
        // Given
        LocalTime expectedStartTime = LocalTime.of(14, 30);
        BookingCreatedEvent domainEvent = new BookingCreatedEvent(
                1L,
                LocalDate.of(2024, 1, 15),
                expectedStartTime,
                LocalTime.of(15, 30)
        );

        // When
        BookingCreatedKafkaEvent kafkaEvent = BookingEventMapper.toKafkaEvent(domainEvent);

        // Then
        assertEquals(expectedStartTime, kafkaEvent.getStartTime());
    }

    @Test
    @DisplayName("Should preserve end time during conversion")
    void shouldPreserveEndTimeDuringConversion() {
        // Given
        LocalTime expectedEndTime = LocalTime.of(16, 45);
        BookingCreatedEvent domainEvent = new BookingCreatedEvent(
                1L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(15, 30),
                expectedEndTime
        );

        // When
        BookingCreatedKafkaEvent kafkaEvent = BookingEventMapper.toKafkaEvent(domainEvent);

        // Then
        assertEquals(expectedEndTime, kafkaEvent.getEndTime());
    }

    @Test
    @DisplayName("Should handle early morning time slots")
    void shouldHandleEarlyMorningTimeSlots() {
        // Given
        BookingCreatedEvent domainEvent = new BookingCreatedEvent(
                1L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(8, 0),
                LocalTime.of(9, 0)
        );

        // When
        BookingCreatedKafkaEvent kafkaEvent = BookingEventMapper.toKafkaEvent(domainEvent);

        // Then
        assertEquals(LocalTime.of(8, 0), kafkaEvent.getStartTime());
        assertEquals(LocalTime.of(9, 0), kafkaEvent.getEndTime());
    }

    @Test
    @DisplayName("Should handle late evening time slots")
    void shouldHandleLateEveningTimeSlots() {
        // Given
        BookingCreatedEvent domainEvent = new BookingCreatedEvent(
                1L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(21, 0),
                LocalTime.of(22, 0)
        );

        // When
        BookingCreatedKafkaEvent kafkaEvent = BookingEventMapper.toKafkaEvent(domainEvent);

        // Then
        assertEquals(LocalTime.of(21, 0), kafkaEvent.getStartTime());
        assertEquals(LocalTime.of(22, 0), kafkaEvent.getEndTime());
    }

    @Test
    @DisplayName("Should create independent Kafka event instance")
    void shouldCreateIndependentKafkaEventInstance() {
        // Given
        BookingCreatedEvent domainEvent = new BookingCreatedEvent(
                1L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        // When
        BookingCreatedKafkaEvent kafkaEvent1 = BookingEventMapper.toKafkaEvent(domainEvent);
        BookingCreatedKafkaEvent kafkaEvent2 = BookingEventMapper.toKafkaEvent(domainEvent);

        // Then
        assertNotSame(kafkaEvent1, kafkaEvent2, "Each conversion should create a new instance");
        assertEquals(kafkaEvent1.getBookingId(), kafkaEvent2.getBookingId());
        assertEquals(kafkaEvent1.getDate(), kafkaEvent2.getDate());
        assertEquals(kafkaEvent1.getStartTime(), kafkaEvent2.getStartTime());
        assertEquals(kafkaEvent1.getEndTime(), kafkaEvent2.getEndTime());
    }
}
