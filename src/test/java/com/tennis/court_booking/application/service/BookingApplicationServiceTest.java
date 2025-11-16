package com.tennis.court_booking.application.service;

import com.tennis.court_booking.application.port.in.BookingResponse;
import com.tennis.court_booking.application.port.in.ReserveCommand;
import com.tennis.court_booking.application.port.out.BookingEventPublisher;
import com.tennis.court_booking.application.port.out.BookingRepository;
import com.tennis.court_booking.domain.entity.Booking;
import com.tennis.court_booking.domain.event.BookingCreatedEvent;
import com.tennis.court_booking.domain.exception.BusinessException;
import com.tennis.court_booking.domain.exception.InvalidTimeSlotException;
import com.tennis.court_booking.domain.service.BookingDomainService;
import com.tennis.court_booking.domain.valueobject.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingApplicationServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingEventPublisher eventPublisher;

    @Mock
    private BookingDomainService domainService;

    private BookingApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new BookingApplicationService(
                bookingRepository,
                eventPublisher,
                domainService
        );
    }

    @Test
    @DisplayName("Should create application service with valid dependencies")
    void shouldCreateApplicationServiceWithValidDependencies() {
        assertNotNull(applicationService);
    }

    @Test
    @DisplayName("Should throw exception when BookingRepository is null")
    void shouldThrowExceptionWhenBookingRepositoryIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BookingApplicationService(null, eventPublisher, domainService)
        );
        assertEquals("BookingRepository cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when BookingEventPublisher is null")
    void shouldThrowExceptionWhenBookingEventPublisherIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BookingApplicationService(bookingRepository, null, domainService)
        );
        assertEquals("BookingEventPublisher cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when BookingDomainService is null")
    void shouldThrowExceptionWhenBookingDomainServiceIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BookingApplicationService(bookingRepository, eventPublisher, null)
        );
        assertEquals("BookingDomainService cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should successfully reserve a booking and return response")
    void shouldSuccessfullyReserveBookingAndReturnResponse() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        ReserveCommand command = new ReserveCommand(date, startTime, endTime);

        TimeSlot timeSlot = new TimeSlot(date, startTime, endTime);
        Booking unsavedBooking = new Booking(null, timeSlot);
        Booking savedBooking = new Booking(1L, timeSlot);

        List<Booking> existingBookings = new ArrayList<>();

        when(bookingRepository.findByDate(date)).thenReturn(existingBookings);
        when(domainService.reserve(any(TimeSlot.class), eq(existingBookings))).thenReturn(unsavedBooking);
        when(bookingRepository.save(unsavedBooking)).thenReturn(savedBooking);

        BookingResponse response = applicationService.reserve(command);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(date, response.getDate());
        assertEquals(startTime, response.getStartTime());
        assertEquals(endTime, response.getEndTime());

        verify(bookingRepository).findByDate(date);
        verify(domainService).reserve(any(TimeSlot.class), eq(existingBookings));
        verify(bookingRepository).save(unsavedBooking);
        verify(eventPublisher).publish(any(BookingCreatedEvent.class));
    }

    @Test
    @DisplayName("Should call domain service with correct TimeSlot")
    void shouldCallDomainServiceWithCorrectTimeSlot() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime startTime = LocalTime.of(14, 30);
        LocalTime endTime = LocalTime.of(15, 30);
        ReserveCommand command = new ReserveCommand(date, startTime, endTime);

        TimeSlot timeSlot = new TimeSlot(date, startTime, endTime);
        Booking unsavedBooking = new Booking(null, timeSlot);
        Booking savedBooking = new Booking(2L, timeSlot);

        List<Booking> existingBookings = new ArrayList<>();

        when(bookingRepository.findByDate(date)).thenReturn(existingBookings);
        when(domainService.reserve(any(TimeSlot.class), eq(existingBookings))).thenReturn(unsavedBooking);
        when(bookingRepository.save(unsavedBooking)).thenReturn(savedBooking);

        applicationService.reserve(command);

        ArgumentCaptor<TimeSlot> timeSlotCaptor = ArgumentCaptor.forClass(TimeSlot.class);
        verify(domainService).reserve(timeSlotCaptor.capture(), eq(existingBookings));

        TimeSlot capturedTimeSlot = timeSlotCaptor.getValue();
        assertEquals(date, capturedTimeSlot.getDate());
        assertEquals(startTime, capturedTimeSlot.getStart());
        assertEquals(endTime, capturedTimeSlot.getEnd());
    }

    @Test
    @DisplayName("Should retrieve existing bookings for the requested date")
    void shouldRetrieveExistingBookingsForRequestedDate() {
        LocalDate date = LocalDate.of(2024, 1, 20);
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(10, 0);
        ReserveCommand command = new ReserveCommand(date, startTime, endTime);

        TimeSlot timeSlot = new TimeSlot(date, startTime, endTime);
        Booking unsavedBooking = new Booking(null, timeSlot);
        Booking savedBooking = new Booking(3L, timeSlot);

        TimeSlot existingSlot1 = new TimeSlot(date, LocalTime.of(11, 0), LocalTime.of(12, 0));
        TimeSlot existingSlot2 = new TimeSlot(date, LocalTime.of(13, 0), LocalTime.of(14, 0));
        List<Booking> existingBookings = List.of(
                new Booking(10L, existingSlot1),
                new Booking(11L, existingSlot2)
        );

        when(bookingRepository.findByDate(date)).thenReturn(existingBookings);
        when(domainService.reserve(any(TimeSlot.class), eq(existingBookings))).thenReturn(unsavedBooking);
        when(bookingRepository.save(unsavedBooking)).thenReturn(savedBooking);

        applicationService.reserve(command);

        verify(bookingRepository).findByDate(date);
        verify(domainService).reserve(any(TimeSlot.class), eq(existingBookings));
    }

    @Test
    @DisplayName("Should save booking returned by domain service")
    void shouldSaveBookingReturnedByDomainService() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime startTime = LocalTime.of(16, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        ReserveCommand command = new ReserveCommand(date, startTime, endTime);

        TimeSlot timeSlot = new TimeSlot(date, startTime, endTime);
        Booking unsavedBooking = new Booking(null, timeSlot);
        Booking savedBooking = new Booking(4L, timeSlot);

        List<Booking> existingBookings = new ArrayList<>();

        when(bookingRepository.findByDate(date)).thenReturn(existingBookings);
        when(domainService.reserve(any(TimeSlot.class), eq(existingBookings))).thenReturn(unsavedBooking);
        when(bookingRepository.save(unsavedBooking)).thenReturn(savedBooking);

        applicationService.reserve(command);

        verify(bookingRepository).save(unsavedBooking);
    }

    @Test
    @DisplayName("Should publish BookingCreatedEvent with correct data")
    void shouldPublishBookingCreatedEventWithCorrectData() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime startTime = LocalTime.of(18, 0);
        LocalTime endTime = LocalTime.of(19, 0);
        ReserveCommand command = new ReserveCommand(date, startTime, endTime);

        TimeSlot timeSlot = new TimeSlot(date, startTime, endTime);
        Booking unsavedBooking = new Booking(null, timeSlot);
        Booking savedBooking = new Booking(5L, timeSlot);

        List<Booking> existingBookings = new ArrayList<>();

        when(bookingRepository.findByDate(date)).thenReturn(existingBookings);
        when(domainService.reserve(any(TimeSlot.class), eq(existingBookings))).thenReturn(unsavedBooking);
        when(bookingRepository.save(unsavedBooking)).thenReturn(savedBooking);

        applicationService.reserve(command);

        ArgumentCaptor<BookingCreatedEvent> eventCaptor = ArgumentCaptor.forClass(BookingCreatedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());

        BookingCreatedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(5L, capturedEvent.getBookingId());
        assertEquals(date, capturedEvent.getDate());
        assertEquals(startTime, capturedEvent.getStartTime());
        assertEquals(endTime, capturedEvent.getEndTime());
    }

    @Test
    @DisplayName("Should return response with ID from saved booking")
    void shouldReturnResponseWithIdFromSavedBooking() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime startTime = LocalTime.of(20, 0);
        LocalTime endTime = LocalTime.of(21, 0);
        ReserveCommand command = new ReserveCommand(date, startTime, endTime);

        TimeSlot timeSlot = new TimeSlot(date, startTime, endTime);
        Booking unsavedBooking = new Booking(null, timeSlot);
        Booking savedBooking = new Booking(100L, timeSlot);

        List<Booking> existingBookings = new ArrayList<>();

        when(bookingRepository.findByDate(date)).thenReturn(existingBookings);
        when(domainService.reserve(any(TimeSlot.class), eq(existingBookings))).thenReturn(unsavedBooking);
        when(bookingRepository.save(unsavedBooking)).thenReturn(savedBooking);

        BookingResponse response = applicationService.reserve(command);

        assertEquals(100L, response.getId());
    }


    @Test
    @DisplayName("Should propagate InvalidTimeSlotException from TimeSlot constructor")
    void shouldPropagateInvalidTimeSlotExceptionFromTimeSlotConstructor() {
        // Given - invalid time slot (end before start)
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime startTime = LocalTime.of(15, 0);
        LocalTime endTime = LocalTime.of(14, 0);
        ReserveCommand command = new ReserveCommand(date, startTime, endTime);

        assertThrows(InvalidTimeSlotException.class, () -> applicationService.reserve(command));

        verifyNoInteractions(bookingRepository);
        verifyNoInteractions(domainService);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Should propagate BusinessException from domain service")
    void shouldPropagateBusinessExceptionFromDomainService() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        ReserveCommand command = new ReserveCommand(date, startTime, endTime);

        List<Booking> existingBookings = new ArrayList<>();

        when(bookingRepository.findByDate(date)).thenReturn(existingBookings);
        when(domainService.reserve(any(TimeSlot.class), eq(existingBookings)))
                .thenThrow(new BusinessException("Opening hours violation"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> applicationService.reserve(command)
        );
        assertEquals("Opening hours violation", exception.getMessage());

        verify(bookingRepository).findByDate(date);
        verify(domainService).reserve(any(TimeSlot.class), eq(existingBookings));
        verify(bookingRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("Should not publish event if save fails")
    void shouldNotPublishEventIfSaveFails() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        ReserveCommand command = new ReserveCommand(date, startTime, endTime);

        TimeSlot timeSlot = new TimeSlot(date, startTime, endTime);
        Booking unsavedBooking = new Booking(null, timeSlot);

        List<Booking> existingBookings = new ArrayList<>();

        when(bookingRepository.findByDate(date)).thenReturn(existingBookings);
        when(domainService.reserve(any(TimeSlot.class), eq(existingBookings))).thenReturn(unsavedBooking);
        when(bookingRepository.save(unsavedBooking)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> applicationService.reserve(command));

        verifyNoInteractions(eventPublisher);
    }


    @Test
    @DisplayName("Should execute complete flow in correct order")
    void shouldExecuteCompleteFlowInCorrectOrder() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        ReserveCommand command = new ReserveCommand(date, startTime, endTime);

        TimeSlot timeSlot = new TimeSlot(date, startTime, endTime);
        Booking unsavedBooking = new Booking(null, timeSlot);
        Booking savedBooking = new Booking(1L, timeSlot);

        List<Booking> existingBookings = new ArrayList<>();

        when(bookingRepository.findByDate(date)).thenReturn(existingBookings);
        when(domainService.reserve(any(TimeSlot.class), eq(existingBookings))).thenReturn(unsavedBooking);
        when(bookingRepository.save(unsavedBooking)).thenReturn(savedBooking);

        BookingResponse response = applicationService.reserve(command);

        var inOrder = inOrder(bookingRepository, domainService, eventPublisher);
        inOrder.verify(bookingRepository).findByDate(date);
        inOrder.verify(domainService).reserve(any(TimeSlot.class), eq(existingBookings));
        inOrder.verify(bookingRepository).save(unsavedBooking);
        inOrder.verify(eventPublisher).publish(any(BookingCreatedEvent.class));

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    @DisplayName("Should handle empty existing bookings list")
    void shouldHandleEmptyExistingBookingsList() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        ReserveCommand command = new ReserveCommand(date, startTime, endTime);

        TimeSlot timeSlot = new TimeSlot(date, startTime, endTime);
        Booking unsavedBooking = new Booking(null, timeSlot);
        Booking savedBooking = new Booking(1L, timeSlot);

        List<Booking> emptyList = new ArrayList<>();

        when(bookingRepository.findByDate(date)).thenReturn(emptyList);
        when(domainService.reserve(any(TimeSlot.class), eq(emptyList))).thenReturn(unsavedBooking);
        when(bookingRepository.save(unsavedBooking)).thenReturn(savedBooking);

        BookingResponse response = applicationService.reserve(command);

        assertNotNull(response);
        verify(domainService).reserve(any(TimeSlot.class), eq(emptyList));
    }
}
