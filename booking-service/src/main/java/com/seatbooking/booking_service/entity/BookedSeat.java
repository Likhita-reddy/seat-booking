package com.seatbooking.booking_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "booked_seats")
@Data
public class BookedSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "seat_id")
    private Long seatId;

    @Column(name = "price_at_booking")
    private BigDecimal priceAtBooking;
}