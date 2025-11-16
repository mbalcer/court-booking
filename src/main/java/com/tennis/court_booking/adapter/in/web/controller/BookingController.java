package com.tennis.court_booking.adapter.in.web.controller;

import com.tennis.court_booking.adapter.in.web.dto.ReserveBookingRequest;
import com.tennis.court_booking.adapter.in.web.dto.ReserveBookingResponse;
import com.tennis.court_booking.application.port.in.BookingResponse;
import com.tennis.court_booking.application.port.in.BookingUseCase;
import com.tennis.court_booking.application.port.in.ReserveCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing court bookings.
 * This is the inbound REST adapter that translates HTTP requests
 * into application use case calls.
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingUseCase bookingUseCase;

    /**
     * Creates a new booking controller.
     *
     * @param bookingUseCase the use case for booking operations
     * @throws IllegalArgumentException if bookingUseCase is null
     */
    public BookingController(BookingUseCase bookingUseCase) {
        if (bookingUseCase == null) {
            throw new IllegalArgumentException("BookingUseCase cannot be null");
        }
        this.bookingUseCase = bookingUseCase;
    }

    /**
     * Creates a new court booking reservation.
     *
     * @param request the booking request containing date and time details
     * @return ResponseEntity with the created booking details and HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<ReserveBookingResponse> reserveBooking(@RequestBody ReserveBookingRequest request) {
        // Map REST request DTO to application command DTO
        ReserveCommand command = new ReserveCommand(
                request.getDate(),
                request.getStart(),
                request.getEnd()
        );

        // Execute the use case
        BookingResponse response = bookingUseCase.reserve(command);

        // Map application response DTO to REST response DTO
        ReserveBookingResponse restResponse = new ReserveBookingResponse(
                response.getId(),
                response.getDate(),
                response.getStartTime(),
                response.getEndTime()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(restResponse);
    }
}
