package com.seatbooking.booking_service.exception;

public class ShowNotFoundException extends RuntimeException{
    public ShowNotFoundException(String message){
        super(message);
    }
}
