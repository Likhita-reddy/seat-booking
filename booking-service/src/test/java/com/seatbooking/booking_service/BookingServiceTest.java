package com.seatbooking.booking_service;

import com.seatbooking.booking_service.dto.LockSeatsRequest;
import com.seatbooking.booking_service.entity.Seat;
import com.seatbooking.booking_service.exception.SeatLockFailedException;
import com.seatbooking.booking_service.exception.SeatNotAvailableException;
import com.seatbooking.booking_service.repository.*;
import com.seatbooking.booking_service.service.BookingService;
import com.seatbooking.booking_service.service.PaymentClient;
import com.seatbooking.booking_service.service.SeatLockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private SeatRepository seatRepository;
    @Mock private SeatCategoryRepository seatCategoryRepository;
    @Mock private ShowRepository showRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private BookedSeatRepository bookedSeatRepository;
    @Mock private SeatLockService seatLockService;
    @Mock private PaymentClient paymentClient;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void lockSeats_seatNotAvailable_throwsException() {
        LockSeatsRequest request = new LockSeatsRequest();
        request.setShowId(1L);
        request.setSeatIds(List.of(1L));

        Seat seat = new Seat();
        seat.setId(1L);
        seat.setSeatNumber("V1");
        seat.setStatus("LOCKED"); // already locked

        when(seatRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(seat));

        assertThrows(SeatNotAvailableException.class,
                () -> bookingService.lockSeats(1L, request));

        // Redis should never be called — rejected at DB check
        verify(seatLockService, never())
                .lockSeatsAtomically(any(), any(), any());
    }

    @Test
    void lockSeats_redisLockFailed_throwsException() {
        LockSeatsRequest request = new LockSeatsRequest();
        request.setShowId(1L);
        request.setSeatIds(List.of(1L));

        Seat seat = new Seat();
        seat.setId(1L);
        seat.setSeatNumber("V1");
        seat.setStatus("AVAILABLE");

        when(seatRepository.findAllById(List.of(1L)))
                .thenReturn(List.of(seat));
        when(seatLockService.lockSeatsAtomically(1L, List.of(1L), 1L))
                .thenReturn(false); // Redis lock failed

        assertThrows(SeatLockFailedException.class,
                () -> bookingService.lockSeats(1L, request));
    }

    @Test
    void lockSeats_seatsNotFound_throwsException() {
        LockSeatsRequest request = new LockSeatsRequest();
        request.setShowId(1L);
        request.setSeatIds(List.of(1L, 2L));

        // Only returns 1 seat when 2 were requested
        Seat seat = new Seat();
        seat.setId(1L);
        seat.setStatus("AVAILABLE");

        when(seatRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(seat));

        assertThrows(
                com.seatbooking.booking_service.exception.BookingNotFoundException.class,
                () -> bookingService.lockSeats(1L, request));
    }
}