package com.seatbooking.payment_service.service;

import com.seatbooking.payment_service.config.WebhookProperties;
import com.seatbooking.payment_service.dto.PaymentInitiateRequest;
import com.seatbooking.payment_service.dto.WebhookPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final RestTemplate restTemplate;
    private final WebhookProperties webhookProperties;

    @Value("${webhook.secret}")
    private String webhookSecret;

    @Async
    public void processPaymentAsync(PaymentInitiateRequest request){
        log.info("Payment processing started in background for booking {}",request.getBookingId());

        WebhookPayload payload;

        try{
            int delay = 2000 + new Random().nextInt(3000);
            Thread.sleep(delay);

            boolean success = new Random().nextDouble() > 0.3;

            if(success){
                log.info("Payment success for booking {} after {} ms",request.getBookingId(),delay);
                payload = new WebhookPayload(
                        request.getBookingId(),
                        request.getUserId(),
                        request.getShowId(),
                        request.getAmount(),
                        "SUCCESS",
                        null,
                        request.getSeatIds()
                );
            } else{
                log.warn("Payment failed for booking {} after {} ms", request.getBookingId(),delay);
                payload = new WebhookPayload(
                        request.getBookingId(),
                        request.getUserId(),
                        request.getShowId(),
                        request.getAmount(),
                        "FAILURE",
                        "Insufficient funds",
                        request.getSeatIds()
                );
            }

        } catch(Exception e){
            Thread.currentThread().interrupt();
            log.error("Payment processing interrupted for booking {}", request.getBookingId());
            payload = new WebhookPayload(
                    request.getBookingId(),
                    request.getUserId(),
                    request.getShowId(),
                    request.getAmount(),
                    "FAILED",
                    "Payment processing interrupted",
                    request.getSeatIds()
            );
        }
        sendWebhookWithRetry(payload);
    }

    private void sendWebhookWithRetry(WebhookPayload payload){
        int maxRetries = webhookProperties.getMaxRetries();
        long backoffMs = webhookProperties.getInitialBackoffMs();

        for(int attempt = 1; attempt <= maxRetries; attempt++){
            try{
                log.info("Sending webhook for booking {} attempt {} maxRetries {}",payload.getBookingId(),attempt,maxRetries);

                HttpHeaders headers = new HttpHeaders();
                headers.set("X-Webhook-Secret",webhookSecret);
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<WebhookPayload> entity = new HttpEntity<>(payload,headers);

                ResponseEntity<Void> response = restTemplate.postForEntity(
                        webhookProperties.getUrl(),
                        entity,
                        Void.class
                );

                if (response.getStatusCode().is2xxSuccessful()) {
                    log.info("Webhook delivered for booking {} on attempt {}",
                            payload.getBookingId(), attempt);
                    return;
                }
                log.warn("Webhook got non-2xx {} for booking {} attempt {}",
                        response.getStatusCode(), payload.getBookingId(), attempt);
            } catch (Exception e) {
                log.warn("Webhook attempt {}/{} failed for booking {} reason: {}",
                        attempt, maxRetries, payload.getBookingId(), e.getMessage());
            }
            if (attempt < maxRetries) {
                try {
                    log.info("Waiting {}ms before retry for booking {}",
                            backoffMs, payload.getBookingId());
                    Thread.sleep(backoffMs);
                    backoffMs *= 2;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Retry wait interrupted for booking {}",
                            payload.getBookingId());
                    break;
                }
            }
        }
        log.error("CRITICAL — Webhook failed after {} attempts for booking {}. " +
                        "Payment status was {}. Manual intervention required.",
                maxRetries, payload.getBookingId(), payload.getStatus());
    }
}
