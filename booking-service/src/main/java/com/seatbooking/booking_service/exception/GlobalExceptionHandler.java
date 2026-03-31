package com.seatbooking.booking_service.exception;

import com.seatbooking.booking_service.dto.ErrorResponse;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(SeatNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleSeatNotAvailable(SeatNotAvailableException e){
        log.warn("Seat not available: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        409,
                        "SEAT NOT AVAILABLE",
                        e.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(SeatLockFailedException.class)
    public ResponseEntity<ErrorResponse> handleSeatLockFailed(SeatLockFailedException e){
        log.warn("Seat lock failed: {}",e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        409,
                        "SEAT LOCK FAILED",
                        e.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookingNotFound(BookingNotFoundException e){
        log.warn("Booking not found: {}",e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        404,
                        "BOOKING NOT FOUND",
                        e.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(LockExpiredException.class)
    public ResponseEntity<ErrorResponse> handleLockExpired(LockExpiredException e){
        log.warn("Lock expired: {}",e.getMessage());
        return ResponseEntity
                .status(HttpStatus.GONE)
                .body(new ErrorResponse(
                        410,
                        "LOCK EXPIRED",
                        e.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(ShowNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleShowNotFound(ShowNotFoundException e){
        log.warn("Show not found: {}",e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        404,
                        "SHOW NOT FOUND",
                        e.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException e){
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation Failed: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        400,
                        "Validation failed",
                        message,
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockException e){
        log.warn("Optmistic lock conflict",e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        409,
                        "Concurrent update",
                        "Resource was modified by another request. Please retry",
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceededException(RateLimitExceededException e){
        log.warn("Rate limit exceeded: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ErrorResponse(
                        429,
                        "RATE_LIMIT_EXCEEDED",
                        e.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handlegeneral(Exception e){
        log.error("Unexpected error: {}",e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        500,
                        "Internal error",
                        "Something went wrong. Please try again",
                        LocalDateTime.now()
                ));
    }
}
