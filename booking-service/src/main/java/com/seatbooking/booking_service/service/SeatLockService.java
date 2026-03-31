package com.seatbooking.booking_service.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatLockService {
    private final RedisTemplate<String, String> redisTemplate;

    private static final Long LOCK_TTL_SECONDS = 300L;

    private DefaultRedisScript<Long> lockScript;
    private DefaultRedisScript<Long> releaseScript;
    private DefaultRedisScript<Long> validateScript;

    @PostConstruct
    public void loadScripts(){
        lockScript = new DefaultRedisScript<>();
        lockScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("scripts/lock_seats.lua")));
        lockScript.setResultType(Long.class);

        releaseScript = new DefaultRedisScript<>();
        releaseScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("scripts/release_seats.lua")));
        releaseScript.setResultType(Long.class);

        validateScript = new DefaultRedisScript<>();
        validateScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("scripts/validate_locks.lua")));
        validateScript.setResultType(Long.class);
    }

    private String buildKey(Long showId, Long seatId){
        return "seat:lock:" + showId + ":" + seatId;
    }
    private List<String> buildKeys(Long showId, List<Long> seatIds){
        return seatIds.stream()
                .map(seatId -> buildKey(showId,seatId))
                .collect(Collectors.toList());
    }

    public boolean lockSeatsAtomically(Long showId, List<Long> seatIds, Long userId){
        List<String> keys = buildKeys(showId, seatIds);

        Long result = redisTemplate.execute(
                lockScript,
                keys,
                String.valueOf(showId),
                String.valueOf(LOCK_TTL_SECONDS)
        );

        boolean success = Long.valueOf(1).equals(result);
        if(!success){
            log.warn("Failed to atomically lock seats {} for show {} user {}",seatIds, showId, userId);
        }

        return success;
    }

    public void releaseAllLocks(Long showId, List<Long> seatIds){
        if(seatIds == null || seatIds.isEmpty())
            return;

        List<String> keys = buildKeys(showId, seatIds);
        redisTemplate.execute(releaseScript, keys);
        log.info("Released locks for seats {} in show {}", seatIds, showId);
    }
    
    public boolean validateAllLocksOwned(Long showId, List<Long> seatIds, Long userId){
        List<String> keys = buildKeys(showId, seatIds);
        Long result = redisTemplate.execute(
                validateScript,
                keys,
                String.valueOf(userId)
        );
        return Long.valueOf(1).equals(result);
    }
}
