package com.seatbooking.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentInitiateResponse {
    private Long bookingId;
    private String message;
}
