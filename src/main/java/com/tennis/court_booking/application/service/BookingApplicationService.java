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
 * Orchestrates domain service and outbound ports to fulfill booking reservations.
 */
public class BookingApplicationService implements BookingUseCase {

    private final BookingRepository bookingRepository;
    private final BookingEventPublisher eventPublisher;
    private final BookingDomainService domainService;

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
     * @param command the reservation command
     * @return the booking response with assigned ID and booking details
     * @throws com.tennis.court_booking.domain.exception.InvalidTimeSlotException if the time slot is invalid
     * @throws com.tennis.court_booking.domain.exception.BusinessException if business rules are violated
     */
    @Override
    public BookingResponse reserve(ReserveCommand command) {
        TimeSlot timeSlot = TimeSlotMapper.toTimeSlot(command);
        List<Booking> existingBookings = bookingRepository.findByDate(command.getDate());
        Booking newBooking = domainService.reserve(timeSlot, existingBookings);
        Booking savedBooking = bookingRepository.save(newBooking);

        BookingCreatedEvent event = BookingMapper.toBookingCreatedEvent(savedBooking);
        eventPublisher.publish(event);

        return BookingMapper.toBookingResponse(savedBooking);
    }
}
