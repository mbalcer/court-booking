package com.tennis.court_booking.adapter.out.event;

import com.tennis.court_booking.application.port.out.BookingEventPublisher;
import com.tennis.court_booking.domain.event.BookingCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logging implementation of the BookingEventPublisher port.
 * This is a temporary stub adapter that logs events instead of publishing them.
 *
 * NOTE: This implementation will be replaced with a Kafka-based event publisher
 * adapter in Step 10 (Event Adapter).
 *
 * Useful for development and testing to verify event publishing flow without
 * requiring external message broker infrastructure.
 */
public class LoggingEventPublisher implements BookingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingEventPublisher.class);

    /**
     * Logs a booking created event instead of publishing it to external systems.
     *
     * @param event the booking created event to log
     * @throws IllegalArgumentException if event is null
     */
    @Override
    public void publish(BookingCreatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        log.info("Publishing BookingCreatedEvent: [bookingId={}, date={}, startTime={}, endTime={}]",
                event.getBookingId(),
                event.getDate(),
                event.getStartTime(),
                event.getEndTime());
    }
}
