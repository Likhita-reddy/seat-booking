package com.seatbooking.booking_service.exception;

public class SeatLockFailedException extends RuntimeException{
    public SeatLockFailedException(String message){
        super(message);
    }
}
