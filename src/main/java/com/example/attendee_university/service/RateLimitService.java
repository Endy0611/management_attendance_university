package com.example.attendee_university.service;

public interface RateLimitService {

    void check(String scope, String identifier, int maxRequests, int windowSeconds);

    /**
     * Fixed-window counter for scope only (no identifier) — caps total
     * requests across ALL clients hitting this endpoint, not just one IP.
     */
    void checkGlobal(String scope, int maxRequests, int windowSeconds);
}