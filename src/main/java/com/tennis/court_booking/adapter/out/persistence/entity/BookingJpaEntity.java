package com.tennis.court_booking.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * JPA entity for persisting bookings to the database.
 * This is an outbound adapter entity that maps to the database schema.
 * It is separate from the domain entity to maintain clean hexagonal architecture.
 *
 * Uses an embedded TimeSlot representation (date, start, end) rather than
 * a separate table to keep the persistence model simple.
 */
@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Constructor for creating a new booking entity without ID (for inserts).
     *
     * @param date      the booking date
     * @param startTime the start time
     * @param endTime   the end time
     */
    public BookingJpaEntity(LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingJpaEntity that = (BookingJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "BookingJpaEntity{" +
                "id=" + id +
                ", date=" + date +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
