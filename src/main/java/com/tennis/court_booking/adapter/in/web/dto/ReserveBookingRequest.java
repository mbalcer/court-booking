package com.tennis.court_booking.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * REST API request DTO for creating a new booking.
 * This is the adapter-layer DTO that receives JSON from HTTP requests.
 * It will be mapped to ReserveCommand before passing to the application layer.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReserveBookingRequest {
    private LocalDate date;
    private LocalTime start;
    private LocalTime end;
}
