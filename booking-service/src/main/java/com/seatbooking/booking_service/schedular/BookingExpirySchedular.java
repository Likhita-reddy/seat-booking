package com.seatbooking.booking_service.scheduler;

import com.seatbooking.booking_service.entity.Booking;
import com.seatbooking.booking_service.entity.BookedSeat;
import com.seatbooking.booking_service.service.RepositoryManager;
import com.seatbooking.booking_service.service.SeatLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpirySchedular {

    private final SeatLockService seatLockService;
    private final RepositoryManager repositoryManager;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expirePendingBookings() {
        List<Booking> expiredBookings =
                repositoryManager.findExpiredBookings("PENDING",
                        LocalDateTime.now());

        if (expiredBookings.isEmpty()) return;

        log.info("Found {} expired bookings", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            Booking freshBooking = repositoryManager
                    .findBookingById(booking.getId())
                    .orElse(null);

            if (freshBooking == null
                    || !"PENDING".equals(freshBooking.getStatus())) {
                log.info("Booking {} already processed — skipping",
                        booking.getId());
                continue;
            }

            freshBooking.setStatus("EXPIRED");
            repositoryManager.saveBooking(freshBooking);

            List<BookedSeat> bookedSeats =
                    repositoryManager.findBookedSeatsByBooking(booking.getId());

            List<Long> seatIds = bookedSeats.stream()
                    .map(BookedSeat::getSeatId)
                    .toList();

            if (!seatIds.isEmpty()) {
                repositoryManager.updateSeatsStatus(seatIds, "AVAILABLE");
                seatLockService.releaseAllLocks(booking.getShowId(), seatIds);
            }

            log.info("Expired booking {} — {} seats released",
                    booking.getId(), seatIds.size());
        }
    }
}