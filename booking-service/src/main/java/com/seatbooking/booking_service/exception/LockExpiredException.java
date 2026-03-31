package com.seatbooking.booking_service.exception;

public class LockExpiredException extends RuntimeException{
    public LockExpiredException(String message){
        super(message);
    }
}
