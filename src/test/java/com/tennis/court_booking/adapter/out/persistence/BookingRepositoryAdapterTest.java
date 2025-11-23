package com.tennis.court_booking.adapter.out.persistence;

import com.tennis.court_booking.adapter.out.persistence.entity.BookingJpaEntity;
import com.tennis.court_booking.adapter.out.persistence.repository.BookingJpaRepository;
import com.tennis.court_booking.domain.entity.Booking;
import com.tennis.court_booking.domain.valueobject.TimeSlot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingRepositoryAdapterTest {

    @Mock
    private BookingJpaRepository jpaRepository;

    @InjectMocks
    private BookingRepositoryAdapter adapter;

    @Test
    void shouldThrowExceptionWhenConstructedWithNullRepository() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new BookingRepositoryAdapter(null)
        );

        assertEquals("JPA repository cannot be null", exception.getMessage());
    }

    @Test
    void shouldFindBookingsByDate() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        BookingJpaEntity jpaEntity1 = new BookingJpaEntity(
                1L,
                date,
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );
        BookingJpaEntity jpaEntity2 = new BookingJpaEntity(
                2L,
                date,
                LocalTime.of(14, 0),
                LocalTime.of(15, 0)
        );

        when(jpaRepository.findByDate(date)).thenReturn(Arrays.asList(jpaEntity1, jpaEntity2));

        // When
        List<Booking> bookings = adapter.findByDate(date);

        // Then
        assertNotNull(bookings);
        assertEquals(2, bookings.size());
        assertEquals(1L, bookings.get(0).getId());
        assertEquals(2L, bookings.get(1).getId());
        verify(jpaRepository, times(1)).findByDate(date);
    }

    @Test
    void shouldReturnEmptyListWhenNoBookingsFoundForDate() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        when(jpaRepository.findByDate(date)).thenReturn(List.of());

        // When
        List<Booking> bookings = adapter.findByDate(date);

        // Then
        assertNotNull(bookings);
        assertTrue(bookings.isEmpty());
        verify(jpaRepository, times(1)).findByDate(date);
    }

    @Test
    void shouldThrowExceptionWhenFindByDateWithNullDate() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adapter.findByDate(null)
        );

        assertEquals("Date cannot be null", exception.getMessage());
        verify(jpaRepository, never()).findByDate(any());
    }

    @Test
    void shouldSaveNewBookingWithoutId() {
        // Given
        TimeSlot timeSlot = new TimeSlot(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );
        Booking newBooking = new Booking(null, timeSlot);

        BookingJpaEntity savedJpaEntity = new BookingJpaEntity(
                1L,
                timeSlot.getDate(),
                timeSlot.getStart(),
                timeSlot.getEnd()
        );

        when(jpaRepository.save(any(BookingJpaEntity.class))).thenReturn(savedJpaEntity);

        // When
        Booking savedBooking = adapter.save(newBooking);

        // Then
        assertNotNull(savedBooking);
        assertEquals(1L, savedBooking.getId());
        assertEquals(timeSlot, savedBooking.getTimeSlot());
        verify(jpaRepository, times(1)).save(any(BookingJpaEntity.class));
    }

    @Test
    void shouldSaveExistingBookingWithId() {
        // Given
        TimeSlot timeSlot = new TimeSlot(
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );
        Booking existingBooking = new Booking(1L, timeSlot);

        BookingJpaEntity savedJpaEntity = new BookingJpaEntity(
                1L,
                timeSlot.getDate(),
                timeSlot.getStart(),
                timeSlot.getEnd()
        );

        when(jpaRepository.save(any(BookingJpaEntity.class))).thenReturn(savedJpaEntity);

        // When
        Booking savedBooking = adapter.save(existingBooking);

        // Then
        assertNotNull(savedBooking);
        assertEquals(1L, savedBooking.getId());
        assertEquals(timeSlot, savedBooking.getTimeSlot());
        verify(jpaRepository, times(1)).save(any(BookingJpaEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenSavingNullBooking() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adapter.save(null)
        );

        assertEquals("Booking cannot be null", exception.getMessage());
        verify(jpaRepository, never()).save(any());
    }

    @Test
    void shouldFindBookingById() {
        // Given
        Long id = 1L;
        BookingJpaEntity jpaEntity = new BookingJpaEntity(
                id,
                LocalDate.of(2024, 1, 15),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0)
        );

        when(jpaRepository.findById(id)).thenReturn(Optional.of(jpaEntity));

        // When
        Optional<Booking> result = adapter.findById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(jpaRepository, times(1)).findById(id);
    }

    @Test
    void shouldReturnEmptyOptionalWhenBookingNotFoundById() {
        // Given
        Long id = 1L;
        when(jpaRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<Booking> result = adapter.findById(id);

        // Then
        assertFalse(result.isPresent());
        verify(jpaRepository, times(1)).findById(id);
    }

    @Test
    void shouldThrowExceptionWhenFindByIdWithNullId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adapter.findById(null)
        );

        assertEquals("ID cannot be null", exception.getMessage());
        verify(jpaRepository, never()).findById(any());
    }

    @Test
    void shouldDeleteBookingById() {
        // Given
        Long id = 1L;
        doNothing().when(jpaRepository).deleteById(id);

        // When
        adapter.delete(id);

        // Then
        verify(jpaRepository, times(1)).deleteById(id);
    }

    @Test
    void shouldThrowExceptionWhenDeleteWithNullId() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> adapter.delete(null)
        );

        assertEquals("ID cannot be null", exception.getMessage());
        verify(jpaRepository, never()).deleteById(any());
    }

    @Test
    void shouldHandleMultipleBookingsOnSameDate() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        BookingJpaEntity jpaEntity1 = new BookingJpaEntity(
                1L, date, LocalTime.of(9, 0), LocalTime.of(10, 0)
        );
        BookingJpaEntity jpaEntity2 = new BookingJpaEntity(
                2L, date, LocalTime.of(10, 0), LocalTime.of(11, 0)
        );
        BookingJpaEntity jpaEntity3 = new BookingJpaEntity(
                3L, date, LocalTime.of(14, 0), LocalTime.of(15, 0)
        );

        when(jpaRepository.findByDate(date)).thenReturn(Arrays.asList(jpaEntity1, jpaEntity2, jpaEntity3));

        // When
        List<Booking> bookings = adapter.findByDate(date);

        // Then
        assertEquals(3, bookings.size());
        assertEquals(1L, bookings.get(0).getId());
        assertEquals(2L, bookings.get(1).getId());
        assertEquals(3L, bookings.get(2).getId());
    }

    @Test
    void shouldConvertJpaEntitiesToDomainEntitiesCorrectly() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);
        BookingJpaEntity jpaEntity = new BookingJpaEntity(1L, date, start, end);

        when(jpaRepository.findByDate(date)).thenReturn(List.of(jpaEntity));

        // When
        List<Booking> bookings = adapter.findByDate(date);

        // Then
        assertEquals(1, bookings.size());
        Booking booking = bookings.get(0);
        assertEquals(1L, booking.getId());
        assertEquals(date, booking.getTimeSlot().getDate());
        assertEquals(start, booking.getTimeSlot().getStart());
        assertEquals(end, booking.getTimeSlot().getEnd());
    }
}
