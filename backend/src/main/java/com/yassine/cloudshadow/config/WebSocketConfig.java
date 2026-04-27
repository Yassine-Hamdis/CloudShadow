package com.yassine.cloudshadow.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * ─── What is STOMP? ──────────────────────────────────────────────────
     * STOMP = Simple Text Oriented Messaging Protocol
     * It's a messaging protocol that runs ON TOP of WebSocket.
     *
     * Think of it like this:
     * WebSocket = the road (raw connection)
     * STOMP     = the traffic rules (how messages are structured)
     *
     * STOMP gives us:
     * → Topics (channels) frontend can SUBSCRIBE to
     * → Clean message routing
     * → Easy integration with Spring
     */

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // ── In-memory message broker ───────────────────────────────────────
        // Frontend subscribes to topics starting with /topic
        // Example: /topic/company/1/metrics
        //          /topic/company/1/alerts
        registry.enableSimpleBroker("/topic");

        // ── Application destination prefix ────────────────────────────────
        // Frontend can SEND messages to backend starting with /app
        // Example: /app/ping (future use)
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // ── WebSocket endpoint ─────────────────────────────────────────────
        // Frontend connects to: ws://localhost:8080/ws
        // SockJS fallback for browsers that don't support WebSocket natively
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")   // Update with frontend URL in prod
                .withSockJS();
    }
}