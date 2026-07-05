package com.example.attendee_university.config;

import com.example.attendee_university.annotation.RateLimited;
import com.example.attendee_university.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RateLimited annotation = handlerMethod.getMethodAnnotation(RateLimited.class);
        if (annotation == null) {
            return true;
        }

        String clientIp = resolveClientIp(request);
        rateLimitService.check(annotation.scope(), clientIp, annotation.maxRequests(), annotation.windowSeconds());
        return true;
    }

    /**
     * Behind Nginx Proxy Manager, request.getRemoteAddr() returns the proxy's
     * IP, not the real client's. X-Forwarded-For is set by NPM by default;
     * falls back to getRemoteAddr() for direct/local requests.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}