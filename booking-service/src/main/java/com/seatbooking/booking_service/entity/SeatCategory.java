package com.seatbooking.booking_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name="seat_categories")
@Data
public class SeatCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="show_Id")
    private Long showId;

    private String name;
    private BigDecimal price;

    @Column(name="total_seats")
    private Integer totalSeats;

    @Column(name="available_seats")
    private Integer availableSeats;

    @Version
    private Integer version;
}
