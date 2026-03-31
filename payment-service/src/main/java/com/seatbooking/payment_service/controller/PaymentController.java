package com.seatbooking.payment_service.controller;

import com.seatbooking.payment_service.dto.PaymentInitiateRequest;
import com.seatbooking.payment_service.dto.PaymentInitiateResponse;
import com.seatbooking.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentInitiateResponse> initiatePayment(
            @RequestBody PaymentInitiateRequest request) {

        log.info("Payment initiation received for booking {} amount {}",
                request.getBookingId(), request.getAmount());


        paymentService.processPaymentAsync(request);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(new PaymentInitiateResponse(
                        request.getBookingId(),
                        "Payment is being processed"));
    }
}