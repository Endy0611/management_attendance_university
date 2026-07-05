package com.example.attendee_university.service;

public interface RateLimitService {

    /**
     * Fixed-window counter for (scope + identifier). Throws
     * RateLimitExceededException once the window's max is exceeded.
     */
    void check(String scope, String identifier, int maxRequests, int windowSeconds);
}