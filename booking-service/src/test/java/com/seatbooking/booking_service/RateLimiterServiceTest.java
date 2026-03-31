package com.seatbooking.booking_service;

import com.seatbooking.booking_service.service.RateLimiterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RateLimiterService rateLimiterService;

    @Test
    void firstRequest_allowed() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("rate:lock:1")).thenReturn(1L);

        assertTrue(rateLimiterService.isAllowed(1L));
    }

    @Test
    void fifthRequest_allowed() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("rate:lock:1")).thenReturn(5L);

        assertTrue(rateLimiterService.isAllowed(1L));
    }

    @Test
    void sixthRequest_blocked() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("rate:lock:1")).thenReturn(6L);

        assertFalse(rateLimiterService.isAllowed(1L));
    }
}