package com.chatapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket + STOMP configuration.
 *
 * <p>Registers the SockJS-backed endpoint at {@code /ws} and configures
 * the simple in-memory message broker with destination prefixes
 * {@code /topic} (broadcast) and {@code /queue} (point-to-point).</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure the message broker.
     * <ul>
     *   <li>{@code /topic} — for broadcast messages (e.g. public chat)</li>
     *   <li>{@code /queue} — for user-specific messages</li>
     *   <li>{@code /app}  — application destination prefix for @MessageMapping</li>
     * </ul>
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Register the STOMP endpoint clients connect to.
     * SockJS fallback is enabled so the app works even when
     * native WebSocket is unavailable.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
