package com.seatbooking.booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SeatResponse {
    private Long seatId;
    private String seatNumber;
    private String status;
    private String categoryName;
    private BigDecimal price;
}
