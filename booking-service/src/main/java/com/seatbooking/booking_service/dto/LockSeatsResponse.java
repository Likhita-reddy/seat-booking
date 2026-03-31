package com.seatbooking.booking_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LockSeatsResponse {
    private List<Long> lockedSeatIds;
    private String message;
    private Long expiresInSeconds;
}
