package com.seatbooking.booking_service.service;

import com.seatbooking.booking_service.aop.TrackMetrics;
import com.seatbooking.booking_service.dto.WebhookPayload;
import com.seatbooking.booking_service.entity.*;
import com.seatbooking.booking_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final BookingRepository bookingRepository;
    private final BookedSeatRepository bookedSeatRepository;
    private final SeatLockService seatLockService;
    private final SeatRepository seatRepository;
    private final SeatCategoryRepository seatCategoryRepository;
    private final ShowRepository showRepository;
    private final TransactionHelperService helper;
    private final RepositoryManager repositoryManager;

    @TrackMetrics(operation = "handle_payment_webhook")
    @Transactional
    public void handlePaymentResult(WebhookPayload payload){
        Booking booking = repositoryManager.findBookingById(payload.getBookingId())
                .orElseThrow(()-> new RuntimeException("Booking not found: "+payload.getBookingId()));

        if(!"PENDING".equals(booking.getStatus()) && !"PAYMENT_INITIATED".equals(booking.getStatus())){
            log.warn("Duplicate webhook for booking {} status {} - ignoring,",booking.getId(),booking.getStatus());
            return;
        }

        if("SUCCESS".equals(payload.getStatus())){
            handleSuccess(booking, payload);
        } else{
            handleFailure(booking, payload);
        }
    }

    private void handleSuccess(Booking booking, WebhookPayload payload){
        booking.setStatus("CONFIRMED");
        repositoryManager.saveBooking(booking);

        List<Seat> seats = repositoryManager.findSeatsByIds(payload.getSeatIds());

        List<SeatCategory> categories = repositoryManager
                .findCategoriesByShow(booking.getShowId());
        Map<Long, SeatCategory> categoryMap = categories.stream()
                .collect(Collectors.toMap(SeatCategory::getId, c -> c));
        List<BookedSeat> bookedSeats = new ArrayList<>();
        for(Seat seat: seats){
            BookedSeat bookedSeat = new BookedSeat();
            bookedSeat.setSeatId(seat.getId());
            bookedSeat.setBookingId(booking.getId());
            bookedSeat.setPriceAtBooking(categoryMap.get(seat.getCategoryId()).getPrice());
            bookedSeats.add(bookedSeat);
        }
        repositoryManager.saveBookedSeats(bookedSeats);


        for(Seat seat: seats){
            Seat freshSeat = repositoryManager.findSeatById(seat.getId())
                    .orElseThrow(()->new RuntimeException("Seat not found"));
            freshSeat.setStatus("BOOKED");
            repositoryManager.saveSeat(freshSeat);
        }

        Map<Long, List<Seat>> seatsByCategory = seats.stream()
                .collect(Collectors.groupingBy(Seat::getCategoryId));

        helper.updateSeatCategoryAvailability(seatsByCategory,-1);
        helper.updateShowAvailability(booking.getShowId(),-seats.size());
        

        seatLockService.releaseAllLocks(booking.getShowId(),payload.getSeatIds());

        //outbox to kafka
        log.info("Booking confirmed {}",booking.getId());
    }

    private void handleFailure(Booking booking, WebhookPayload payload){
        log.warn("Booking failed for booking {} reason: {}",booking.getId(),payload.getReason());

        booking.setStatus("FAILED");
        bookingRepository.save(booking);


        List<Long> seatIds = payload.getSeatIds();
        seatRepository.updateStatusByIds(seatIds,"AVAILABLE");
        seatLockService.releaseAllLocks(payload.getShowId(),seatIds);


        seatLockService.releaseAllLocks(booking.getShowId(), seatIds);

        log.warn("Booking {} failed - compensated",booking.getId());
    }


}
