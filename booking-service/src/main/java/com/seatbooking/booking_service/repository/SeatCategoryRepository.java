package com.seatbooking.booking_service.repository;

import com.seatbooking.booking_service.entity.SeatCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatCategoryRepository extends JpaRepository<SeatCategory,Long> {
    List<SeatCategory> findByShowId(Long showId);
    Optional<SeatCategory> findById(Long id);
}
