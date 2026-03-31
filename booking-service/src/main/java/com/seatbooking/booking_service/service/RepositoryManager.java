package com.seatbooking.booking_service.service;

import com.seatbooking.booking_service.aop.TrackDBTime;
import com.seatbooking.booking_service.entity.*;
import com.seatbooking.booking_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RepositoryManager {
    private final SeatRepository seatRepository;
    private final SeatCategoryRepository seatCategoryRepository;
    private final ShowRepository showRepository;
    private final BookingRepository bookingRepository;
    private final BookedSeatRepository bookedSeatRepository;

    @TrackDBTime(operation="find_seats_by_show")
    public List<Seat> findSeatsByShow(Long showId){
        return seatRepository.findByShowId(showId);
    }

    @TrackDBTime(operation="find_seats_by_ids")
    public List<Seat> findSeatsByIds(List<Long> seatIds){
        return seatRepository.findAllById(seatIds);
    }

    @TrackDBTime(operation = "save_seat")
    public Seat saveSeat(Seat seat){
        return seatRepository.save(seat);
    }

    @TrackDBTime(operation="find_seat_by_id")
    public Optional<Seat> findSeatById(Long seatId){
        return seatRepository.findById(seatId);
    }

    @TrackDBTime(operation = "update_seats_status")
    public void updateSeatsStatus(List<Long> seatIds, String status){
        seatRepository.updateStatusByIds(seatIds, status);
    }

    @TrackDBTime(operation = "save_booking")
    public Booking saveBooking(Booking booking){
        return bookingRepository.save(booking);
    }

    @TrackDBTime(operation = "find_booking_by_id")
    public Optional<Booking> findBookingById(Long bookingId){
        return bookingRepository.findById(bookingId);
    }

    @TrackDBTime(operation = "find_expired_bookings")
    public List<Booking> findExpiredBookings(String status, LocalDateTime time){
        return bookingRepository.findByStatusAndExpiresAtBefore(status, time);
    }

    @TrackDBTime(operation = "find_booked_seats")
    public List<BookedSeat> findBookedSeatsByBooking(Long bookingId){
        return bookedSeatRepository.findByBookingId(bookingId);
    }

    @TrackDBTime(operation = "save_booked_seats")
    public void saveBookedSeats(List<BookedSeat> bookedSeats){
        bookedSeatRepository.saveAll(bookedSeats);
    }

    @TrackDBTime(operation = "find_categories_by_show")
    public List<SeatCategory> findCategoriesByShow(Long showId){
        return seatCategoryRepository.findByShowId(showId);
    }

    @TrackDBTime(operation = "find_category_by_id")
    public Optional<SeatCategory> findCategoryById(Long seatCategoryId){
        return seatCategoryRepository.findById(seatCategoryId);
    }

    @TrackDBTime(operation = "save_category")
    public SeatCategory saveCategory(SeatCategory category){
        return seatCategoryRepository.save(category);
    }

    @TrackDBTime(operation = "find_show_by_id")
    public Optional<Show> findShowById(Long showId){
        return showRepository.findById(showId);
    }

    @TrackDBTime(operation = "save_show")
    public Show saveShow(Show show){
        return showRepository.save(show);
    }

    
}
