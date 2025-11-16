package com.tennis.court_booking.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Standardized error response DTO for REST API error handling.
 * Provides consistent error information across all API endpoints.
 */
@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
}
