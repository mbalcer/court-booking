package com.tennis.court_booking.domain.policy;

import com.tennis.court_booking.domain.exception.BusinessException;
import com.tennis.court_booking.domain.valueobject.TimeSlot;
import lombok.Value;

import java.time.LocalTime;

/**
 * Business policy that validates if a booking is within allowed opening hours.
 * Ensures that bookings can only be made within the tennis court's operating hours.
 */
@Value
public class OpeningHoursPolicy {
    LocalTime openingTime;
    LocalTime closingTime;

    /**
     * Creates a new OpeningHoursPolicy with the specified opening and closing times.
     *
     * @param openingTime the time when the court opens
     * @param closingTime the time when the court closes
     * @throws IllegalArgumentException if opening or closing time is null, or if closing time is not after opening time
     */
    public OpeningHoursPolicy(LocalTime openingTime, LocalTime closingTime) {
        if (openingTime == null) {
            throw new IllegalArgumentException("Opening time cannot be null");
        }
        if (closingTime == null) {
            throw new IllegalArgumentException("Closing time cannot be null");
        }
        if (!closingTime.isAfter(openingTime)) {
            throw new IllegalArgumentException("Closing time must be after opening time");
        }

        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    /**
     * Validates if the given time slot is within opening hours.
     *
     * @param timeSlot the time slot to validate
     * @throws BusinessException if the time slot is outside opening hours
     */
    public void validate(TimeSlot timeSlot) {
        if (timeSlot == null) {
            throw new IllegalArgumentException("TimeSlot cannot be null");
        }

        if (timeSlot.getStart().isBefore(openingTime)) {
            throw new BusinessException(
                String.format("Booking cannot start before opening time. Start: %s, Opening time: %s",
                    timeSlot.getStart(), openingTime)
            );
        }

        if (timeSlot.getEnd().isAfter(closingTime)) {
            throw new BusinessException(
                String.format("Booking cannot end after closing time. End: %s, Closing time: %s",
                    timeSlot.getEnd(), closingTime)
            );
        }
    }
}
