package com.example.attendee_university.service.impl;

import com.example.attendee_university.exception.RateLimitExceededException;
import com.example.attendee_university.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        String key = KEY_PREFIX + scope + ":" + identifier;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        if (count != null && count > maxRequests) {
            log.warn("Rate limit exceeded — scope: {}, identifier: {}, count: {}", scope, identifier, count);
            throw new RateLimitExceededException(
                    "Too many requests. Please wait before trying again.");
        }
    }
}