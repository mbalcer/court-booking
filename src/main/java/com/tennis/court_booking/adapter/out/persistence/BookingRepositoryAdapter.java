package com.tennis.court_booking.adapter.out.persistence;

import com.tennis.court_booking.adapter.out.persistence.entity.BookingJpaEntity;
import com.tennis.court_booking.adapter.out.persistence.mapper.BookingPersistenceMapper;
import com.tennis.court_booking.adapter.out.persistence.repository.BookingJpaRepository;
import com.tennis.court_booking.application.port.out.BookingRepository;
import com.tennis.court_booking.domain.entity.Booking;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementation of the BookingRepository port using JPA.
 * This is an outbound adapter that implements the repository interface
 * defined in the application layer, using Spring Data JPA for persistence.
 *
 * Responsibilities:
 * - Delegates persistence operations to Spring Data JPA repository
 * - Converts between domain entities and JPA entities using mapper
 * - Maintains separation between domain and persistence concerns
 */
@Component
public class BookingRepositoryAdapter implements BookingRepository {

    private final BookingJpaRepository jpaRepository;

    /**
     * Constructor for dependency injection.
     *
     * @param jpaRepository the Spring Data JPA repository
     * @throws IllegalArgumentException if jpaRepository is null
     */
    public BookingRepositoryAdapter(BookingJpaRepository jpaRepository) {
        if (jpaRepository == null) {
            throw new IllegalArgumentException("JPA repository cannot be null");
        }
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Booking> findByDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        return jpaRepository.findByDate(date).stream()
                .map(BookingPersistenceMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Booking save(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking cannot be null");
        }

        BookingJpaEntity jpaEntity = BookingPersistenceMapper.toJpaEntity(booking);
        BookingJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        return BookingPersistenceMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Booking> findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        return jpaRepository.findById(id)
                .map(BookingPersistenceMapper::toDomainEntity);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        jpaRepository.deleteById(id);
    }
}
