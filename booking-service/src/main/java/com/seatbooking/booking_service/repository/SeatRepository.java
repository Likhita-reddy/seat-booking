package com.seatbooking.booking_service.repository;

import com.seatbooking.booking_service.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat,Long> {
    List<Seat> findByShowId(Long showId);
    Optional<Seat> findById(Long seatId);

    @Modifying
    @Query("UPDATE Seat s SET s.status = :status, s.version = s.version + 1 WHERE s.id in :seatIds AND s.version = :version")
    int updateStatusByIdsAndVersion(@Param("seatIds") List<Long> seatIds, @Param("status") String status, @Param("version") Integer version);

    @Modifying
    @Query("UPDATE Seat s SET s.status = :status WHERE s.id IN :seatIds")
    void updateStatusByIds(@Param("seatIds") List<Long> seatIds, @Param("status") String status);
}
