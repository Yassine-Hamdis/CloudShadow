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
public class ServerStatusPayload {

    // ─── Structured status payload for WebSocket status messages ───────
    // Replaces the old plain String message
    // Frontend can parse this reliably without string splitting

    private Long serverId;
    private String serverName;

    // "ONLINE" or "OFFLINE"
    private String status;

    // When the server last sent a metric
    // null if server never sent a metric
    private LocalDateTime lastSeen;
}