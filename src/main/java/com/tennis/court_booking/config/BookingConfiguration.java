package com.tennis.court_booking.config;

import com.tennis.court_booking.adapter.out.event.LoggingEventPublisher;
import com.tennis.court_booking.adapter.out.persistence.InMemoryBookingRepository;
import com.tennis.court_booking.application.port.in.BookingUseCase;
import com.tennis.court_booking.application.port.out.BookingEventPublisher;
import com.tennis.court_booking.application.port.out.BookingRepository;
import com.tennis.court_booking.application.service.BookingApplicationService;
import com.tennis.court_booking.domain.policy.OpeningHoursPolicy;
import com.tennis.court_booking.domain.policy.OverlappingReservationsPolicy;
import com.tennis.court_booking.domain.service.BookingDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;

/**
 * Spring configuration class for the booking domain.
 * Wires together all dependencies following hexagonal architecture principles.
 *
 * This configuration:
 * - Defines business policies with configurable parameters
 * - Creates domain service with required policies
 * - Provides stub implementations for outbound ports (repository, event publisher)
 * - Wires the application service that implements the use case
 *
 * NOTE: Repository and event publisher implementations are temporary stubs
 * that will be replaced with real implementations in Steps 9-10.
 */
@Configuration
public class BookingConfiguration {

    /**
     * Configures the opening hours policy for the tennis court.
     * Court is open from 8:00 to 20:00 (8 AM to 8 PM).
     *
     * @return configured opening hours policy
     */
    @Bean
    public OpeningHoursPolicy openingHoursPolicy() {
        return new OpeningHoursPolicy(
                LocalTime.of(8, 0),   // Opening time: 8:00 AM
                LocalTime.of(20, 0)   // Closing time: 8:00 PM
        );
    }

    /**
     * Configures the overlapping reservations policy.
     * Ensures no double bookings for the same time slot.
     *
     * @return overlapping reservations policy
     */
    @Bean
    public OverlappingReservationsPolicy overlappingReservationsPolicy() {
        return new OverlappingReservationsPolicy();
    }

    /**
     * Configures the booking domain service.
     * Orchestrates business logic and coordinates business policies.
     *
     * @param openingHoursPolicy the opening hours policy
     * @param overlappingReservationsPolicy the overlapping reservations policy
     * @return configured booking domain service
     */
    @Bean
    public BookingDomainService bookingDomainService(
            OpeningHoursPolicy openingHoursPolicy,
            OverlappingReservationsPolicy overlappingReservationsPolicy) {
        return new BookingDomainService(openingHoursPolicy, overlappingReservationsPolicy);
    }

    /**
     * Configures the booking repository.
     * Uses in-memory implementation for development/testing.
     *
     * NOTE: This will be replaced with JPA repository implementation in Step 9.
     *
     * @return in-memory booking repository
     */
    @Bean
    public BookingRepository bookingRepository() {
        return new InMemoryBookingRepository();
    }

    /**
     * Configures the booking event publisher.
     * Uses logging implementation for development/testing.
     *
     * NOTE: This will be replaced with Kafka publisher implementation in Step 10.
     *
     * @return logging event publisher
     */
    @Bean
    public BookingEventPublisher bookingEventPublisher() {
        return new LoggingEventPublisher();
    }

    /**
     * Configures the booking application service (use case implementation).
     * Orchestrates the complete booking reservation flow.
     *
     * @param bookingRepository the repository for persistence operations
     * @param eventPublisher the publisher for domain events
     * @param domainService the domain service for business logic
     * @return configured booking application service as BookingUseCase
     */
    @Bean
    public BookingUseCase bookingUseCase(
            BookingRepository bookingRepository,
            BookingEventPublisher eventPublisher,
            BookingDomainService domainService) {
        return new BookingApplicationService(bookingRepository, eventPublisher, domainService);
    }
}
