package com.seatbooking.booking_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTemplate<String,String> redisTemplate;

    private static final int MAX_REQUESTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public boolean isAllowed(Long userId){
        String key = "rate:lock:" + userId;

        Long count = redisTemplate.opsForValue().increment(key);

        if(count == 1){
            redisTemplate.expire(key, WINDOW);
        }

        if(count > MAX_REQUESTS){
            log.warn("Rate limit exceeded for user {} - {} requests in 1 minute", userId, count);
            return false;
        }
        return true;
    }
}
