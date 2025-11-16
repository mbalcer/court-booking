package com.tennis.court_booking.application.mapper;

import com.tennis.court_booking.application.port.in.ReserveCommand;
import com.tennis.court_booking.domain.valueobject.TimeSlot;

/**
 * Mapper for converting ReserveCommand DTO to TimeSlot domain object.
 */
public class TimeSlotMapper {

    private TimeSlotMapper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Converts a ReserveCommand to a TimeSlot.
     *
     * @param command the reserve command
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
