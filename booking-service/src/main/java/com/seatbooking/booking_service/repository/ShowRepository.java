package com.seatbooking.booking_service.repository;

import com.seatbooking.booking_service.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowRepository extends JpaRepository<Show,Long> {
}
