package com.seatbooking.booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class WebhookPayload {
    private Long bookingId;
    private Long userId;
    private Long showId;
    private BigDecimal amount;
    private String status;
    private String reason;
    private List<Long> seatIds;
}
