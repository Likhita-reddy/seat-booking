package com.seatbooking.booking_service.repository;

import com.seatbooking.booking_service.entity.BookedSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookedSeatRepository extends JpaRepository<BookedSeat,Long> {
    List<BookedSeat> findByBookingId(Long bookingId);
}
