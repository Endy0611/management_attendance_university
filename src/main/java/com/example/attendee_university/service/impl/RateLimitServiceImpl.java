package com.example.attendee_university.service.impl;

import com.example.attendee_university.exception.RateLimitExceededException;
import com.example.attendee_university.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {

    private static final String KEY_PREFIX = "ratelimit:";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void check(String scope, String identifier, int maxRequests, int windowSeconds) {
        doCheck(KEY_PREFIX + scope + ":" + identifier, maxRequests, windowSeconds, scope, identifier);
    }

    @Override
    public void checkGlobal(String scope, int maxRequests, int windowSeconds) {
        doCheck(KEY_PREFIX + "global:" + scope, maxRequests, windowSeconds, scope, "GLOBAL");
    }

    private void doCheck(String key, int maxRequests, int windowSeconds, String scope, String identifier) {
        Long count;
        long ttl;

        try {
            count = redisTemplate.opsForValue().increment(key);

            if (count != null && count == 1L) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }

            Long rawTtl = redisTemplate.getExpire(key);
            ttl = (rawTtl != null && rawTtl > 0) ? rawTtl : windowSeconds;

        } catch (RedisConnectionFailureException | RedisSystemException e) {
            // Fail open — connection failures AND timeouts/system errors
            // shouldn't take the whole API down. Better to briefly run
            // unprotected than to go fully unavailable over an infra hiccup.
            log.error("Redis unavailable — rate limiting bypassed for scope: {}, identifier: {}", scope, identifier, e);
            return;
        }

        if (count != null && count > maxRequests) {
            log.warn("Rate limit exceeded — scope: {}, identifier: {}, count: {}", scope, identifier, count);
            throw new RateLimitExceededException(
                    "Too many requests. Please wait before trying again.", ttl);
        }
    }
}