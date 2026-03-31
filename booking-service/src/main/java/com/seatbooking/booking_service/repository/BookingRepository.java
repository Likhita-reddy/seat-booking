package com.seatbooking.booking_service.repository;

import com.seatbooking.booking_service.entity.BookedSeat;
import com.seatbooking.booking_service.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking,Long> {
    List<Booking> findByStatusAndExpiresAtBefore(String status, LocalDateTime time);
}
