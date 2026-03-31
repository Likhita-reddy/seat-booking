package com.seatbooking.payment_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PaymentInitiateRequest {
    private Long bookingId;
    private Long userId;
    private Long showId;
    private BigDecimal amount;
    private List<Long> seatIds;
}
