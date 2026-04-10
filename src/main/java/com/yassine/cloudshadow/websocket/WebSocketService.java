package com.yassine.cloudshadow.websocket;

import com.yassine.cloudshadow.dto.response.AlertResponse;
import com.yassine.cloudshadow.dto.response.MetricResponse;
import com.yassine.cloudshadow.dto.response.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * ─── How topics work ──────────────────────────────────────────────────
     *
     * Each company gets their OWN private topic channel:
     *
     * Company 1 → /topic/company/1/metrics
     *             /topic/company/1/alerts
     *
     * Company 2 → /topic/company/2/metrics
     *             /topic/company/2/alerts
     *
     * Multi-tenant isolation:
     * → Company 1 frontend subscribes to /topic/company/1/**
     * → Company 2 frontend subscribes to /topic/company/2/**
     * → They NEVER see each other's data
     *
     * Frontend subscription example (JavaScript):
     * stompClient.subscribe('/topic/company/1/metrics', (message) => {
     *     const metric = JSON.parse(message.body);
     *     updateChart(metric);
     * });
     */

    // ─── Push New Metric to Company Dashboard ─────────────────────────────
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

    // ─── Push New Alert to Company Dashboard ──────────────────────────────
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

    // ─── Push Server Online Status ─────────────────────────────────────────
    public void pushServerOnline(Long serverId, String serverName,
                                 Long companyId) {
        String topic = buildStatusTopic(companyId);

        WebSocketMessage<String> message = WebSocketMessage.serverOnline(
                "Server " + serverName + " is online", companyId
        );

        messagingTemplate.convertAndSend(topic, message);

        log.info("🟢 Server online pushed → {} [companyId={}]",
                serverName, companyId);
    }

    // ─── Push Server Offline Status ───────────────────────────────────────
    public void pushServerOffline(Long serverId, String serverName,
                                  Long companyId) {
        String topic = buildStatusTopic(companyId);

        WebSocketMessage<String> message = WebSocketMessage.serverOffline(
                "Server " + serverName + " is offline", companyId
        );

        messagingTemplate.convertAndSend(topic, message);

        log.warn("🔴 Server offline pushed → {} [companyId={}]",
                serverName, companyId);
    }

    // ─── Topic Builders ───────────────────────────────────────────────────
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