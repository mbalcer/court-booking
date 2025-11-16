package com.tennis.court_booking.application.service;

import com.tennis.court_booking.application.mapper.BookingMapper;
import com.tennis.court_booking.application.mapper.TimeSlotMapper;
import com.tennis.court_booking.application.port.in.BookingResponse;
import com.tennis.court_booking.application.port.in.BookingUseCase;
import com.tennis.court_booking.application.port.in.ReserveCommand;
import com.tennis.court_booking.application.port.out.BookingEventPublisher;
import com.tennis.court_booking.application.port.out.BookingRepository;
import com.tennis.court_booking.domain.entity.Booking;
import com.tennis.court_booking.domain.event.BookingCreatedEvent;
import com.tennis.court_booking.domain.service.BookingDomainService;
import com.tennis.court_booking.domain.valueobject.TimeSlot;

import java.util.List;

/**
 * Application service implementing the booking use case.
 * This service coordinates between the domain layer and outbound ports
 * to fulfill the booking reservation use case.
 *
 * Responsibilities:
 * 1. Convert command DTOs to domain objects
 * 2. Orchestrate domain service and policies
 * 3. Persist bookings via repository port
 * 4. Publish domain events via event publisher port
 * 5. Convert domain objects to response DTOs
 *
 * This service is part of the application layer and knows about both
 * inbound ports (use cases) and outbound ports (repository, event publisher).
 */
public class BookingApplicationService implements BookingUseCase {

    private final BookingRepository bookingRepository;
    private final BookingEventPublisher eventPublisher;
    private final BookingDomainService domainService;

    /**
     * Creates a new BookingApplicationService with required dependencies.
     *
     * @param bookingRepository the repository for persisting bookings
     * @param eventPublisher the publisher for broadcasting domain events
     * @param domainService the domain service for business logic orchestration
     * @throws IllegalArgumentException if any dependency is null
     */
    public BookingApplicationService(
            BookingRepository bookingRepository,
            BookingEventPublisher eventPublisher,
            BookingDomainService domainService) {
        if (bookingRepository == null) {
            throw new IllegalArgumentException("BookingRepository cannot be null");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("BookingEventPublisher cannot be null");
        }
        if (domainService == null) {
            throw new IllegalArgumentException("BookingDomainService cannot be null");
        }

        this.bookingRepository = bookingRepository;
        this.eventPublisher = eventPublisher;
        this.domainService = domainService;
    }

    /**
     * Reserves a new court booking for the specified time slot.
     *
     * This implementation:
     * 1. Converts the command to a domain TimeSlot using TimeSlotMapper
     * 2. Retrieves existing bookings for the date
     * 3. Delegates to domain service for business rule validation
     * 4. Persists the booking and obtains the assigned ID
     * 5. Publishes a booking created event using BookingMapper
     * 6. Returns a booking response DTO using BookingMapper
     *
     * @param command the reservation command containing date, start, and end times
     * @return the booking response with assigned ID and booking details
     * @throws com.tennis.court_booking.domain.exception.InvalidTimeSlotException if the time slot is invalid
     * @throws com.tennis.court_booking.domain.exception.BusinessException if business rules are violated
     */
    @Override
    public BookingResponse reserve(ReserveCommand command) {
        // 1. Convert command to domain TimeSlot using mapper
        TimeSlot timeSlot = TimeSlotMapper.toTimeSlot(command);

        // 2. Retrieve existing bookings for the date
        List<Booking> existingBookings = bookingRepository.findByDate(command.getDate());

        // 3. Use domain service to create booking (validates business rules)
        // Domain service returns booking with null ID
        Booking newBooking = domainService.reserve(timeSlot, existingBookings);

        // 4. Persist the booking - repository assigns the ID
        Booking savedBooking = bookingRepository.save(newBooking);

        // 5. Publish domain event using mapper
        BookingCreatedEvent event = BookingMapper.toBookingCreatedEvent(savedBooking);
        eventPublisher.publish(event);

        // 6. Convert domain entity to response DTO using mapper
        return BookingMapper.toBookingResponse(savedBooking);
    }
}
