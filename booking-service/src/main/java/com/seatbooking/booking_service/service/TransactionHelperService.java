package com.seatbooking.booking_service.service;

import com.seatbooking.booking_service.entity.Booking;
import com.seatbooking.booking_service.entity.Seat;
import com.seatbooking.booking_service.entity.SeatCategory;
import com.seatbooking.booking_service.entity.Show;
import com.seatbooking.booking_service.repository.BookingRepository;
import com.seatbooking.booking_service.repository.SeatCategoryRepository;
import com.seatbooking.booking_service.repository.SeatRepository;
import com.seatbooking.booking_service.repository.ShowRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionHelperService {

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final SeatCategoryRepository seatCategoryRepository;
    private final ShowRepository showRepository;
    private final RepositoryManager repositoryManager;

    @Transactional
    public Booking createPendingBooking(Long userId, Long showId, BigDecimal totalAmount){
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setShowId(showId);
        booking.setStatus("PENDING");
        booking.setTotalAmount(totalAmount);
        return repositoryManager.saveBooking(booking);
    }

    @Transactional
    public void markPaymentInitiated(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus("PAYMENT_INITIATED");
        repositoryManager.saveBooking(booking);
    }

    @Transactional
    public void updateSeatStatus(List<Long> seatIds, String status){
        repositoryManager.updateSeatsStatus(seatIds, status);
    }

    @Transactional
    public void updateSeatCategoryAvailability(Map<Long, List<Seat>> seatsByCategory, int delta){
        seatsByCategory.forEach(
                (categoryId,categorySeats)->{
                    int maxRetries = 3;
                    int attempt = 0;

                    while(attempt<maxRetries){
                        try{
                            SeatCategory freshCategory = repositoryManager.findCategoryById(categoryId)
                                    .orElseThrow(()-> new RuntimeException("Seat category not found"));
                            freshCategory.setAvailableSeats(
                                    freshCategory.getAvailableSeats() + (delta * categorySeats.size())
                            );
                            repositoryManager.saveCategory(freshCategory);
                            return;
                        }catch (jakarta.persistence.OptimisticLockException e) {
                            attempt++;
                            log.warn("Optimistic lock conflict on category {} attempt {}/{}",
                                    categoryId, attempt, maxRetries);

                            if (attempt >= maxRetries) {
                                log.error("CRITICAL — Failed to update category {} " +
                                        "after {} attempts", categoryId, maxRetries);
                                throw new RuntimeException(
                                        "Category update failed after retries");
                            }
                            try { Thread.sleep(50L * attempt); }
                            catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
        );
    }

    @Transactional
    public void updateShowAvailability(Long showId, int delta){
        int maxRetries = 3;
        int attempt = 0;

        while(attempt < maxRetries){
            try{
                Show show = repositoryManager.findShowById(showId)
                        .orElseThrow(()-> new RuntimeException("Show not found"));
                show.setAvailableSeats(show.getAvailableSeats()+delta);
                repositoryManager.saveShow(show);
                return;
            } catch(OptimisticLockException e){
                attempt++;
                log.warn("Optimistic lock conflict on show {} attempt {}/{}",showId, attempt, maxRetries);

                if (attempt >= maxRetries) {
                    log.error("CRITICAL — Failed to update show {} after {} attempts",
                            showId, maxRetries);
                    throw new RuntimeException("Show update failed after retries");
                }

                try { Thread.sleep(50L * attempt); }
                catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

    }
}
