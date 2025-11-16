package com.tennis.court_booking.application.mapper;

import com.tennis.court_booking.application.port.in.ReserveCommand;
import com.tennis.court_booking.domain.valueobject.TimeSlot;

/**
 * Mapper for converting ReserveCommand DTO to TimeSlot value object.
 * This mapper is part of the application layer and handles the translation
 * between application-layer DTOs and domain objects.
 *
 * Responsibilities:
 * - Convert ReserveCommand DTO to TimeSlot value object
 *
 * This maintains clean boundaries in the hexagonal architecture by keeping
 * the conversion logic separate from the application service.
 */
public class TimeSlotMapper {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private TimeSlotMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Converts a ReserveCommand DTO to a TimeSlot value object.
     * The TimeSlot constructor will validate the input data and throw
     * InvalidTimeSlotException if the data is invalid.
     *
     * @param command the reserve command containing date, start time, and end time
     * @return a TimeSlot value object
     * @throws IllegalArgumentException if command is null
     * @throws com.tennis.court_booking.domain.exception.InvalidTimeSlotException if the time slot data is invalid
     */
    public static TimeSlot toTimeSlot(ReserveCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("ReserveCommand cannot be null");
        }

        return new TimeSlot(
                command.getDate(),
                command.getStart(),
                command.getEnd()
        );
    }
}
