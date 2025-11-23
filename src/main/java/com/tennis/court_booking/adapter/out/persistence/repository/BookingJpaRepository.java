package com.tennis.court_booking.adapter.out.persistence.repository;

import com.tennis.court_booking.adapter.out.persistence.entity.BookingJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for BookingJpaEntity.
 * Provides CRUD operations and custom queries for booking persistence.
 *
 * This is a Spring-specific infrastructure interface that will be
 * wrapped by the BookingRepositoryAdapter to implement the domain's
 * BookingRepository port interface.
 */
@Repository
public interface BookingJpaRepository extends JpaRepository<BookingJpaEntity, Long> {

    /**
     * Finds all bookings for a specific date.
     * Spring Data JPA will automatically implement this method based on the naming convention.
     *
     * @param date the booking date to search for
     * @return list of booking entities on the specified date
     */
    List<BookingJpaEntity> findByDate(LocalDate date);
}
