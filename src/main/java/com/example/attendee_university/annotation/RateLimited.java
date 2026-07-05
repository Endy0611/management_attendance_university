package com.example.attendee_university.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    int maxRequests();
    int windowSeconds();
    /** Included in the Redis key alongside client IP, e.g. "login", "verify-otp" — keeps counters independent per endpoint. */
    String scope();
}