package com.seatbooking.booking_service.service;

import com.seatbooking.booking_service.dto.PaymentInitiateRequest;
import com.seatbooking.booking_service.dto.PaymentInitiateResponse;
import com.seatbooking.booking_service.dto.PaymentRequest;
import com.seatbooking.booking_service.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentClient {
    private final RestTemplate restTemplate;

    @Value("${payment.service.url}")
    private String paymentServiceUrl;

    public void initiatePayment(PaymentInitiateRequest request){
        try{
            ResponseEntity<PaymentInitiateResponse> response = restTemplate.postForEntity(
                    paymentServiceUrl + "/payment/initiate",
                    request,
                    PaymentInitiateResponse.class
            );

            if(!response.getStatusCode().is2xxSuccessful()){
                throw new Exception("Payment service returned: "+response.getStatusCode());
            }
            log.info("Payment initiation accepted for booking {}", request.getBookingId());
        } catch(Exception e){
            log.error("Payment service unreachable for booking {}",request.getBookingId(),e);
            throw new RuntimeException("Payment service unreachable. Please try again");
        }
    }

    public PaymentResponse processPayment(PaymentRequest request){
        return restTemplate.postForObject(
                paymentServiceUrl+"/payment/process",
                request,
                PaymentResponse.class
        );
    }
}
