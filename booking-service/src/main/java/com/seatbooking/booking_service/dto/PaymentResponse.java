package com.seatbooking.booking_service.dto;

import lombok.Data;

@Data
public class PaymentResponse {
    private String status;
    private Long bookingId;
}
