package com.seatbooking.booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BookingResponse {
    private Long BookingId;
    private String status;
    private BigDecimal totalAmount;
    private String message;
}
