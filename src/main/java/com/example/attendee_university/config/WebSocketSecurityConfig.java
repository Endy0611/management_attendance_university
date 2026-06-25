package com.example.attendee_university.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Authorization rules for STOMP message channels.
     * All subscriptions and sends require authentication.
     * The actual JWT principal is set by JwtHandshakeInterceptor at connection time.
     */
    @Bean
    AuthorizationManager<Message<?>> messageAuthorizationManager(
            MessageMatcherDelegatingAuthorizationManager.Builder messages) {

        messages
                // Attendance events — any authenticated user
                .simpSubscribeDestMatchers("/topic/sessions/*/attendance").authenticated()
                // Per-user notifications
                .simpSubscribeDestMatchers("/user/queue/notifications").authenticated()
                // Session live status
                .simpSubscribeDestMatchers("/topic/sessions/*/status").authenticated()
                // Deny everything else by default
                .anyMessage().authenticated();

        return messages.build();
    }
}