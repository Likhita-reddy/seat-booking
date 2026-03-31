package com.seatbooking.booking_service.controller;

import com.seatbooking.booking_service.dto.*;
import com.seatbooking.booking_service.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BookingController {

    @Value("${HOSTNAME:local}")
    private String hostName;

    private final BookingService bookingService;

    @GetMapping("/shows/{showId}/seats")
    public ResponseEntity<List<SeatResponse>> getSeats(@PathVariable Long showId){
        log.info("Request handled by instance: {}", hostName);
        return ResponseEntity.ok(bookingService.getSeatsForShow(showId));
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingStatus(Authentication authentication, @PathVariable Long bookingId){
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(
                bookingService.getBookingStatus(userId, bookingId)
        );
    }

    @PostMapping("/bookings/lock")
    public ResponseEntity<LockSeatsResponse> lockSeats(Authentication authentication, @Valid @RequestBody LockSeatsRequest request){
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(bookingService.lockSeats(userId, request));
    }

    @PostMapping("/bookings/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(Authentication authentication, @Valid @RequestBody ConfirmBookingRequest request){
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(bookingService.confirmBooking(userId,request));
    }


}
