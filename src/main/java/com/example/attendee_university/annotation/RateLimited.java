package com.example.attendee_university.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    int maxRequests();
    int windowSeconds();
    String scope();

    /** 0 = no global cap for this endpoint. Set > 0 to enable. */
    int globalMaxRequests() default 0;
    int globalWindowSeconds() default 0;
}