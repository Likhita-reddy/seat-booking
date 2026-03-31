package com.seatbooking.booking_service.controller;

import com.seatbooking.booking_service.dto.WebhookPayload;
import com.seatbooking.booking_service.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/bookings/webhook")
@RequiredArgsConstructor
public class WebhookController {
    private final WebhookService webhookService;

    @Value("${webhook.secret}")
    private String webhookSecret;

    @PostMapping("/payment")
    public ResponseEntity<Void> handlePaymentWebhook(@RequestHeader(value="X-Webhook-Secret", required = false) String incomingSecret, @RequestBody WebhookPayload payload){

        if(incomingSecret == null || !incomingSecret.equals(webhookSecret)){
            log.warn("Unauthorized webhook attempt for booking {} - invalid secret", payload.getBookingId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        log.info("Webhook received for booking {} status {}",payload.getBookingId(),payload.getStatus());
        try{
            webhookService.handlePaymentResult(payload);
            return ResponseEntity.ok().build();
        } catch(Exception e){
            log.error("Webhook processing failed for booking {}. Retrying...",payload.getBookingId(),e);
            return ResponseEntity.internalServerError().build();
        }


    }
}
