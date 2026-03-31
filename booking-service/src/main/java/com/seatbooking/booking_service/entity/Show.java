package com.seatbooking.booking_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name="shows")
@Data
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String venue;

    @Column(name="show_date")
    private LocalDateTime showDate;

    @Column(name="total_seats")
    private Integer totalSeats;

    @Column(name="available_seats")
    private Integer availableSeats;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Version
    private Integer version;
}
