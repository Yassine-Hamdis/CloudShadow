package com.yassine.cloudshadow.websocket;

import com.yassine.cloudshadow.dto.response.AlertResponse;
import com.yassine.cloudshadow.dto.response.MetricResponse;
import com.yassine.cloudshadow.dto.response.ServerStatusPayload;
import com.yassine.cloudshadow.dto.response.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    // ─── Push New Metric to Company Dashboard ──────────────────────────
    public void pushMetric(MetricResponse metric, Long companyId) {
        String topic = buildMetricTopic(companyId);

        WebSocketMessage<MetricResponse> message =
                WebSocketMessage.newMetric(metric, companyId);

        messagingTemplate.convertAndSend(topic, message);

        log.debug(
                "📡 Pushed metric to {} → Server: {} | CPU: {}% | MEM: {}%",
                topic,
                metric.getServerName(),
                metric.getCpu(),
                metric.getMemory()
        );
    }

    // ─── Push New Alert to Company Dashboard ───────────────────────────
    public void pushAlert(AlertResponse alert, Long companyId) {
        String topic = buildAlertTopic(companyId);

        WebSocketMessage<AlertResponse> message =
                WebSocketMessage.newAlert(alert, companyId);

        messagingTemplate.convertAndSend(topic, message);

        log.info(
                "🚨 Pushed alert to {} → Server: {} | Type: {} | Severity: {}",
                topic,
                alert.getServerName(),
                alert.getType(),
                alert.getSeverity()
        );
    }

    // ─── Push Server Online Status ─────────────────────────────────────
    // Called when first metric arrives after server was offline/new
    public void pushServerOnline(Long serverId, String serverName,
                                 Long companyId, LocalDateTime lastSeen) {

        String topic = buildStatusTopic(companyId);

        // ── Build structured payload (replaces plain String) ───────────
        ServerStatusPayload payload = ServerStatusPayload.builder()
                .serverId(serverId)
                .serverName(serverName)
                .status("ONLINE")
                .lastSeen(lastSeen)
                .build();

        WebSocketMessage<ServerStatusPayload> message =
                WebSocketMessage.serverOnline(payload, companyId);

        messagingTemplate.convertAndSend(topic, message);

        log.info("🟢 Server online → {} [companyId={}]",
                serverName, companyId);
    }

    // ─── Push Server Offline Status ────────────────────────────────────
    // Called by scheduler when server stops sending metrics
    public void pushServerOffline(Long serverId, String serverName,
                                  Long companyId, LocalDateTime lastSeen) {

        String topic = buildStatusTopic(companyId);

        // ── Build structured payload (replaces plain String) ───────────
        ServerStatusPayload payload = ServerStatusPayload.builder()
                .serverId(serverId)
                .serverName(serverName)
                .status("OFFLINE")
                .lastSeen(lastSeen)
                .build();

        WebSocketMessage<ServerStatusPayload> message =
                WebSocketMessage.serverOffline(payload, companyId);

        messagingTemplate.convertAndSend(topic, message);

        log.warn("🔴 Server offline → {} [companyId={}]",
                serverName, companyId);
    }

    // ─── Topic Builders ────────────────────────────────────────────────
    private String buildMetricTopic(Long companyId) {
        return String.format("/topic/company/%d/metrics", companyId);
    }

    private String buildAlertTopic(Long companyId) {
        return String.format("/topic/company/%d/alerts", companyId);
    }

    private String buildStatusTopic(Long companyId) {
        return String.format("/topic/company/%d/status", companyId);
    }
}