package com.tennis.court_booking.adapter.out.event;

import com.tennis.court_booking.adapter.out.event.dto.BookingCreatedKafkaEvent;
import com.tennis.court_booking.adapter.out.event.mapper.BookingEventMapper;
import com.tennis.court_booking.application.port.out.BookingEventPublisher;
import com.tennis.court_booking.domain.event.BookingCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka adapter implementation of the BookingEventPublisher port.
 * This outbound adapter publishes domain events to Kafka topics.
 *
 * Responsibilities:
 * 1. Implements the BookingEventPublisher port interface
 * 2. Converts domain events to Kafka-specific DTOs using BookingEventMapper
 * 3. Publishes events to configured Kafka topics using KafkaTemplate
 * 4. Handles publishing success and failure with appropriate logging
 * 5. Validates inputs to ensure no null events are published
 *
 * Key design principles:
 * - Thin adapter with no business logic
 * - Delegates conversion to mapper
 * - Uses dependency injection for configuration (topic name)
 * - Async publishing with callback handling
 * - Comprehensive error logging for troubleshooting
 */
@Slf4j
@Component
public class BookingEventPublisherAdapter implements BookingEventPublisher {

    private final KafkaTemplate<String, BookingCreatedKafkaEvent> kafkaTemplate;
    private final String bookingCreatedTopic;

    /**
     * Creates a new Kafka event publisher adapter.
     *
     * @param kafkaTemplate the Spring Kafka template for publishing messages
     * @param bookingCreatedTopic the topic name for booking created events (injected from configuration)
     * @throws IllegalArgumentException if kafkaTemplate or bookingCreatedTopic is null
     */
    public BookingEventPublisherAdapter(
            KafkaTemplate<String, BookingCreatedKafkaEvent> kafkaTemplate,
            @Value("${kafka.topic.booking-created:booking-created}") String bookingCreatedTopic) {
        if (kafkaTemplate == null) {
            throw new IllegalArgumentException("KafkaTemplate cannot be null");
        }
        if (bookingCreatedTopic == null || bookingCreatedTopic.isBlank()) {
            throw new IllegalArgumentException("Booking created topic cannot be null or blank");
        }
        this.kafkaTemplate = kafkaTemplate;
        this.bookingCreatedTopic = bookingCreatedTopic;
        log.info("BookingEventPublisherAdapter initialized with topic: {}", bookingCreatedTopic);
    }

    /**
     * Publishes a booking created event to Kafka.
     * The event is converted to a Kafka-specific DTO and sent to the configured topic.
     * Uses the booking ID as the message key for partitioning.
     *
     * Publishing is asynchronous. Success and failure are logged but not propagated
     * to maintain loose coupling with event consumers.
     *
     * @param event the domain event to publish
     * @throws IllegalArgumentException if event is null
     */
    @Override
    public void publish(BookingCreatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        log.debug("Publishing booking created event for booking ID: {}", event.getBookingId());

        // Convert domain event to Kafka-specific DTO
        BookingCreatedKafkaEvent kafkaEvent = BookingEventMapper.toKafkaEvent(event);

        // Use booking ID as message key for consistent partitioning
        String messageKey = event.getBookingId().toString();

        // Send to Kafka asynchronously
        CompletableFuture<SendResult<String, BookingCreatedKafkaEvent>> future =
                kafkaTemplate.send(bookingCreatedTopic, messageKey, kafkaEvent);

        // Handle success and failure callbacks
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published booking created event for booking ID: {} to topic: {} partition: {} offset: {}",
                        event.getBookingId(),
                        bookingCreatedTopic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish booking created event for booking ID: {} to topic: {}",
                        event.getBookingId(),
                        bookingCreatedTopic,
                        ex);
            }
        });
    }
}
