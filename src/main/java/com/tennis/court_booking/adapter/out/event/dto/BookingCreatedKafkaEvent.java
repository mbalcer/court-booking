package com.tennis.court_booking.adapter.out.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Kafka-specific event DTO for booking creation events.
 * This is the data structure that will be serialized and sent to Kafka topics.
 *
 * This DTO is separate from the domain event to:
 * 1. Keep domain layer independent of infrastructure concerns
 * 2. Allow versioning and evolution of the message format
 * 3. Add Kafka/JSON-specific annotations without polluting domain
 * 4. Enable backward/forward compatibility with consumers
 *
 * Uses Jackson annotations for JSON serialization/deserialization.
 */
@Getter
@NoArgsConstructor  // Required for JSON deserialization
@AllArgsConstructor
public class BookingCreatedKafkaEvent {

    @JsonProperty("booking_id")
    private Long bookingId;

    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonProperty("start_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @JsonProperty("end_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;
}
