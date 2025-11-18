package com.tennis.court_booking.adapter.out.event;

import com.tennis.court_booking.adapter.out.event.dto.BookingCreatedKafkaEvent;
import com.tennis.court_booking.domain.event.BookingCreatedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookingEventPublisherAdapter.
 * Tests the Kafka event publishing functionality using mocked KafkaTemplate.
 *
 * Test coverage:
 * - Constructor validation (null checks)
 * - Successful event publishing
 * - Event publishing with correct parameters (topic, key, value)
 * - Null event validation
 * - Proper delegation to KafkaTemplate
 * - Callback handling (success scenarios)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingEventPublisherAdapter Tests")
class BookingEventPublisherAdapterTest {

    @Mock
    private KafkaTemplate<String, BookingCreatedKafkaEvent> kafkaTemplate;

    private BookingEventPublisherAdapter adapter;

    private static final String TEST_TOPIC = "test-booking-created-topic";

    @BeforeEach
    void setUp() {
        adapter = new BookingEventPublisherAdapter(kafkaTemplate, TEST_TOPIC);
    }

    @Test
    @DisplayName("Should throw exception when KafkaTemplate is null")
    void shouldThrowExceptionWhenKafkaTemplateIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BookingEventPublisherAdapter(null, TEST_TOPIC)
        );

        assertEquals("KafkaTemplate cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when topic name is null")
    void shouldThrowExceptionWhenTopicNameIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BookingEventPublisherAdapter(kafkaTemplate, null)
        );

        assertEquals("Booking created topic cannot be null or blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when topic name is blank")
    void shouldThrowExceptionWhenTopicNameIsBlank() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BookingEventPublisherAdapter(kafkaTemplate, "   ")
        );

        assertEquals("Booking created topic cannot be null or blank", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when event is null")
    void shouldThrowExceptionWhenEventIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adapter.publish(null)
        );

        assertEquals("Event cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should successfully publish valid event to Kafka")
    void shouldSuccessfullyPublishValidEvent() {
        // Given
        BookingCreatedEvent domainEvent = new BookingCreatedEvent(
                1L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        CompletableFuture<SendResult<String, BookingCreatedKafkaEvent>> future =
                CompletableFuture.completedFuture(createSuccessfulSendResult());

        when(kafkaTemplate.send(anyString(), anyString(), any(BookingCreatedKafkaEvent.class)))
                .thenReturn(future);

        // When
        adapter.publish(domainEvent);

        // Then
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(BookingCreatedKafkaEvent.class));
    }

    @Test
    @DisplayName("Should use booking ID as message key")
    void shouldUseBookingIdAsMessageKey() {
        // Given
        Long expectedBookingId = 42L;
        BookingCreatedEvent domainEvent = new BookingCreatedEvent(
                expectedBookingId,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        CompletableFuture<SendResult<String, BookingCreatedKafkaEvent>> future =
                CompletableFuture.completedFuture(createSuccessfulSendResult());

        when(kafkaTemplate.send(anyString(), anyString(), any(BookingCreatedKafkaEvent.class)))
                .thenReturn(future);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);

        // When
        adapter.publish(domainEvent);

        // Then
        verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), any(BookingCreatedKafkaEvent.class));
        assertEquals(expectedBookingId.toString(), keyCaptor.getValue());
    }

    @Test
    @DisplayName("Should publish to correct topic")
    void shouldPublishToCorrectTopic() {
        // Given
        BookingCreatedEvent domainEvent = new BookingCreatedEvent(
                1L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        CompletableFuture<SendResult<String, BookingCreatedKafkaEvent>> future =
                CompletableFuture.completedFuture(createSuccessfulSendResult());

        when(kafkaTemplate.send(anyString(), anyString(), any(BookingCreatedKafkaEvent.class)))
                .thenReturn(future);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);

        // When
        adapter.publish(domainEvent);

        // Then
        verify(kafkaTemplate).send(topicCaptor.capture(), anyString(), any(BookingCreatedKafkaEvent.class));
        assertEquals(TEST_TOPIC, topicCaptor.getValue());
    }

    @Test
    @DisplayName("Should convert domain event to Kafka event with correct data")
    void shouldConvertDomainEventToKafkaEventWithCorrectData() {
        // Given
        BookingCreatedEvent domainEvent = new BookingCreatedEvent(
                1L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        CompletableFuture<SendResult<String, BookingCreatedKafkaEvent>> future =
                CompletableFuture.completedFuture(createSuccessfulSendResult());

        when(kafkaTemplate.send(anyString(), anyString(), any(BookingCreatedKafkaEvent.class)))
                .thenReturn(future);

        ArgumentCaptor<BookingCreatedKafkaEvent> eventCaptor =
                ArgumentCaptor.forClass(BookingCreatedKafkaEvent.class);

        // When
        adapter.publish(domainEvent);

        // Then
        verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

        BookingCreatedKafkaEvent capturedEvent = eventCaptor.getValue();
        assertEquals(domainEvent.getBookingId(), capturedEvent.getBookingId());
        assertEquals(domainEvent.getDate(), capturedEvent.getDate());
        assertEquals(domainEvent.getStartTime(), capturedEvent.getStartTime());
        assertEquals(domainEvent.getEndTime(), capturedEvent.getEndTime());
    }

    @Test
    @DisplayName("Should handle multiple publish calls")
    void shouldHandleMultiplePublishCalls() {
        // Given
        BookingCreatedEvent event1 = new BookingCreatedEvent(
                1L, LocalDate.of(2024, 1, 15), LocalTime.of(10, 0), LocalTime.of(11, 0)
        );
        BookingCreatedEvent event2 = new BookingCreatedEvent(
                2L, LocalDate.of(2024, 1, 15), LocalTime.of(11, 0), LocalTime.of(12, 0)
        );

        CompletableFuture<SendResult<String, BookingCreatedKafkaEvent>> future =
                CompletableFuture.completedFuture(createSuccessfulSendResult());

        when(kafkaTemplate.send(anyString(), anyString(), any(BookingCreatedKafkaEvent.class)))
                .thenReturn(future);

        // When
        adapter.publish(event1);
        adapter.publish(event2);

        // Then
        verify(kafkaTemplate, times(2)).send(anyString(), anyString(), any(BookingCreatedKafkaEvent.class));
    }

    @Test
    @DisplayName("Should not throw exception when Kafka send fails")
    void shouldNotThrowExceptionWhenKafkaSendFails() {
        // Given
        BookingCreatedEvent domainEvent = new BookingCreatedEvent(
                1L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        CompletableFuture<SendResult<String, BookingCreatedKafkaEvent>> future =
                new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka connection failed"));

        when(kafkaTemplate.send(anyString(), anyString(), any(BookingCreatedKafkaEvent.class)))
                .thenReturn(future);

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> adapter.publish(domainEvent));
    }

    @Test
    @DisplayName("Should use default topic when not specified")
    void shouldUseDefaultTopicWhenNotSpecified() {
        // Given
        String defaultTopic = "booking-created";
        BookingEventPublisherAdapter adapterWithDefault =
                new BookingEventPublisherAdapter(kafkaTemplate, defaultTopic);

        BookingCreatedEvent domainEvent = new BookingCreatedEvent(
                1L,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        CompletableFuture<SendResult<String, BookingCreatedKafkaEvent>> future =
                CompletableFuture.completedFuture(createSuccessfulSendResult());

        when(kafkaTemplate.send(anyString(), anyString(), any(BookingCreatedKafkaEvent.class)))
                .thenReturn(future);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);

        // When
        adapterWithDefault.publish(domainEvent);

        // Then
        verify(kafkaTemplate).send(topicCaptor.capture(), anyString(), any(BookingCreatedKafkaEvent.class));
        assertEquals(defaultTopic, topicCaptor.getValue());
    }

    /**
     * Helper method to create a successful SendResult for testing.
     */
    private SendResult<String, BookingCreatedKafkaEvent> createSuccessfulSendResult() {
        ProducerRecord<String, BookingCreatedKafkaEvent> producerRecord =
                new ProducerRecord<>(TEST_TOPIC, "1", new BookingCreatedKafkaEvent());

        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(TEST_TOPIC, 0),
                0L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );

        return new SendResult<>(producerRecord, metadata);
    }
}
