package com.yassine.cloudshadow.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Component
public class WebSocketEventListener {

    /**
     * Tracks WebSocket connections and disconnections.
     * Useful for:
     * → Monitoring how many clients are connected
     * → Debugging connection issues
     * → Future: presence detection (who is online)
     */

    // ─── Client Connected ─────────────────────────────────────────────────
    @EventListener
    public void handleWebSocketConnectListener(
            SessionConnectedEvent event) {

        StompHeaderAccessor headerAccessor =
                StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        log.info("🔌 New WebSocket connection → Session ID: {}", sessionId);
    }

    // ─── Client Subscribed to Topic ───────────────────────────────────────
    @EventListener
    public void handleWebSocketSubscribeListener(
            SessionSubscribeEvent event) {

        StompHeaderAccessor headerAccessor =
                StompHeaderAccessor.wrap(event.getMessage());

        String sessionId    = headerAccessor.getSessionId();
        String destination  = headerAccessor.getDestination();

        log.info("📋 Client subscribed → Session: {} | Topic: {}",
                sessionId, destination);
    }

    // ─── Client Disconnected ──────────────────────────────────────────────
    @EventListener
    public void handleWebSocketDisconnectListener(
            SessionDisconnectEvent event) {

        StompHeaderAccessor headerAccessor =
                StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        log.info("🔌 WebSocket disconnected → Session ID: {}", sessionId);
    }
}