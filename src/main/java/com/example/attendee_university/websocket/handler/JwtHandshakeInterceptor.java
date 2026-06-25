package com.example.attendee_university.websocket.handler;

import com.example.attendee_university.jwt.JwtService;
import com.example.attendee_university.service.AppUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Intercepts the WebSocket upgrade request, extracts and validates the JWT
 * from the Authorization header (or ?token= query param for SockJS fallback),
 * and stores the authenticated principal in the WS session attributes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final AppUserService appUserService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        String token = extractToken(request);

        if (token == null) {
            log.warn("[WS] Handshake rejected — no token provided");
            return false;
        }

        try {
            String email = jwtService.extractUsername(token);
            UserDetails userDetails = appUserService.loadUserByUsername(email);

            if (!jwtService.validateToken(token, userDetails)) {
                log.warn("[WS] Handshake rejected — invalid token for {}", email);
                return false;
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // Spring will use this principal for @MessageMapping routing and /user/... destinations
            attributes.put("principal", auth);
            log.info("[WS] Handshake accepted for {}", email);
            return true;

        } catch (Exception e) {
            log.warn("[WS] Handshake rejected — token error: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // nothing to do after handshake
    }

    private String extractToken(ServerHttpRequest request) {
        // 1. Try Authorization: Bearer <token> header
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. Fallback: ?token=<token> query param (needed for SockJS polling transports)
        String query = request.getURI().getQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) {
                    return param.substring(6);
                }
            }
        }

        return null;
    }
}