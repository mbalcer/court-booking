package com.tennis.court_booking.application.port.out;

import com.tennis.court_booking.application.event.BookingCreatedEvent;

/**
 * Outbound port (event publisher interface) for publishing booking-related events.
 * This interface defines the contract for publishing domain events to external systems
 * (e.g., message queues, event streams).
 *
 * In hexagonal architecture, this is a secondary/driven port that will be
 * implemented by an outbound adapter (e.g., Kafka event publisher adapter).
 * The application layer depends on this interface, not on the implementation.
 */
public interface BookingEventPublisher {

    /**
     * Publishes a booking created event to external systems.
     * This is typically used to notify other services or trigger downstream processes
     * when a new booking has been successfully created and persisted.
     *
     * @param event the booking created event to publish
     * @throws RuntimeException if the event cannot be published
     */
    void publish(BookingCreatedEvent event);
}
