package com.yassine.cloudshadow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage<T> {

    /**
     * Type of message — frontend uses this to decide what to do:
     * "NEW_METRIC"     → update charts
     * "NEW_ALERT"      → show notification
     * "SERVER_ONLINE"  → update server status badge
     * "SERVER_OFFLINE" → update server status badge
     */
    private String type;

    // The actual data payload
    private T data;

    // Company ID — frontend verifies it's for their company
    private Long companyId;

    // When this message was sent
    private LocalDateTime timestamp;

    // ─── Factory Methods ──────────────────────────────────────────────────

    public static <T> WebSocketMessage<T> newMetric(T data, Long companyId) {
        return WebSocketMessage.<T>builder()
                .type("NEW_METRIC")
                .data(data)
                .companyId(companyId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> WebSocketMessage<T> newAlert(T data, Long companyId) {
        return WebSocketMessage.<T>builder()
                .type("NEW_ALERT")
                .data(data)
                .companyId(companyId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> WebSocketMessage<T> serverOnline(T data, Long companyId) {
        return WebSocketMessage.<T>builder()
                .type("SERVER_ONLINE")
                .data(data)
                .companyId(companyId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> WebSocketMessage<T> serverOffline(T data, Long companyId) {
        return WebSocketMessage.<T>builder()
                .type("SERVER_OFFLINE")
                .data(data)
                .companyId(companyId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}