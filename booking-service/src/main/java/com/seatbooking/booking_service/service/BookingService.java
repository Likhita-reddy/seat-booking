package com.seatbooking.booking_service.service;

import com.seatbooking.booking_service.aop.TrackMetrics;
import com.seatbooking.booking_service.dto.*;
import com.seatbooking.booking_service.entity.*;
import com.seatbooking.booking_service.exception.*;
import com.seatbooking.booking_service.repository.*;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final SeatRepository seatRepository;
    private final SeatCategoryRepository seatCategoryRepository;
    private final BookingRepository bookingRepository;
    private final BookedSeatRepository bookedSeatRepository;
    private final ShowRepository showRepository;
    private final RepositoryManager repositoryManager;

    private final SeatLockService seatLockService;
    private final PaymentClient paymentClient;
    private final TransactionHelperService helper;
    private final RateLimiterService rateLimiterService;


    @TrackMetrics(operation = "get_seats_for_show")
    public List<SeatResponse> getSeatsForShow(Long showId){
        List<Seat> seats = repositoryManager.findSeatsByShow(showId);
        List<SeatCategory> categories =repositoryManager.findCategoriesByShow(showId);

        Map<Long, SeatCategory> categoryMap = categories.stream()
                .collect(Collectors.toMap(SeatCategory::getId,c->c));

        return seats.stream()
                .map(seat->{
                    SeatCategory category = categoryMap.get(seat.getCategoryId());
                    return new SeatResponse(
                            seat.getId(),
                            seat.getSeatNumber(),
                            seat.getStatus(),
                            category.getName(),
                            category.getPrice()
                    );
                }).collect(Collectors.toList());
    }

    @TrackMetrics(operation = "get_booking_status", trackCount = false)
    public BookingResponse getBookingStatus(Long userId, Long bookingId){
        Booking booking = repositoryManager
                .findBookingById(bookingId)
                .orElseThrow(()->new BookingNotFoundException("Booking not found with id: "+bookingId));
        if(!booking.getUserId().equals(userId)){
            throw new BookingNotFoundException("Booking doesn't belong to you");
        }
        return new BookingResponse(
                booking.getId(),
                booking.getStatus(),
                booking.getTotalAmount(),
                "Booking status: "+booking.getStatus()
        );
    }

    @TrackMetrics(operation = "lock_seats")
    public LockSeatsResponse lockSeats(Long userId, LockSeatsRequest request){

        if(!rateLimiterService.isAllowed(userId)){
            throw new RateLimitExceededException("Too many lock requests. Maximum 5 per minute allowed");
        }
        List<Seat> seats = repositoryManager.findSeatsByIds(request.getSeatIds());

        if(seats.size() != request.getSeatIds().size()){
            throw new BookingNotFoundException("One or more seats not found");
        }

        for(Seat seat : seats){
            if(!"AVAILABLE".equals(seat.getStatus())){
                throw new SeatNotAvailableException("Seat "+seat.getSeatNumber()+" is not available");
            }
        }

        boolean locked = seatLockService.lockSeatsAtomically(request.getShowId(), request.getSeatIds(), userId);

        if(!locked){
            throw new SeatLockFailedException("One or more seats just locked by another user. Please try again");
        }

        try{
            helper.updateSeatStatus(request.getSeatIds(),"LOCKED");
        } catch(Exception e){
            log.error("DB update failed after Redis lock acquired. Compensating Redis.",e);
            seatLockService.releaseAllLocks(request.getShowId(), request.getSeatIds());
            throw new SeatNotAvailableException("Failed to lock seats. Please try again");
        }

        return new LockSeatsResponse(request.getSeatIds(),"Seats locked successfully",300L);

    }

    @TrackMetrics(operation = "confirm_booking")
    public BookingResponse confirmBooking(Long userId, ConfirmBookingRequest request){
        boolean isValid = seatLockService.validateAllLocksOwned(request.getShowId(),request.getSeatIds(),userId);

        if(!isValid){
            throw new LockExpiredException("Seat locks expired or do not belong to you. Please lock seats again");
        }

        List<Seat> seats = repositoryManager.findSeatsByIds(request.getSeatIds());
        List<SeatCategory> seatCategory = repositoryManager.findCategoriesByShow(request.getShowId());

        Map<Long, SeatCategory> categoryMap = seatCategory.stream()
                .collect(Collectors.toMap(SeatCategory::getId,c->c));

        BigDecimal totalAmount = calculateTotalAmount(seats, categoryMap);

        Booking savedBooking = helper.createPendingBooking(userId, request.getShowId(), totalAmount);

        try{
            PaymentInitiateRequest paymentRequest = new PaymentInitiateRequest(
                    savedBooking.getId(), userId, request.getShowId(),totalAmount,request.getSeatIds());
            paymentClient.initiatePayment(paymentRequest);
            helper.markPaymentInitiated(savedBooking.getId());
        } catch (Exception e){
            log.error("Payment initiation failed for booking {}",savedBooking.getId());
            throw new RuntimeException("Payment service unavailable. Please try again");
        }

        return new BookingResponse(
                savedBooking.getId(),
                "PENDING",
                totalAmount,
                "Payment is being processed"+"Check booking status"+savedBooking.getId()
        );

    }

    private BigDecimal calculateTotalAmount(List<Seat> seats, Map<Long, SeatCategory> categoryMap){
        return seats.stream()
                .map(seat->{
                    SeatCategory seatCategory = categoryMap.get(seat.getCategoryId());
                    if(seatCategory == null){
                        throw new BookingNotFoundException("Category not found for seat: "+seat.getSeatNumber());
                    }
                    return seatCategory.getPrice();
                })
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }





}
