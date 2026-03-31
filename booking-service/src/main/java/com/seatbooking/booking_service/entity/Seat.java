package com.seatbooking.booking_service.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="seats")
@Data
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "show_id")
    private Long showId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "seat_number")
    private String seatNumber;

    private String status;

    @Version
    private Integer version;
}
