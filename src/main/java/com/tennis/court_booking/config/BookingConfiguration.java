package com.tennis.court_booking.config;

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
 * - Wires the application service that implements the use case
 *
 * Note: Adapter implementations (BookingRepositoryAdapter, BookingEventPublisherAdapter)
 * are auto-detected via @Component scanning and injected automatically.
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
     * Configures the booking application service (use case implementation).
     * Orchestrates the complete booking reservation flow.
     *
     * The repository and event publisher are injected automatically from their
     * @Component implementations (BookingRepositoryAdapter and BookingEventPublisherAdapter).
     *
     * @param bookingRepository the repository for persistence operations (auto-injected)
     * @param eventPublisher the publisher for domain events (auto-injected)
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
