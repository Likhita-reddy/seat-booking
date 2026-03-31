package com.seatbooking.booking_service.exception;

public class SeatNotAvailableException extends RuntimeException{
    public SeatNotAvailableException(String message){
        super(message);
    }
}
