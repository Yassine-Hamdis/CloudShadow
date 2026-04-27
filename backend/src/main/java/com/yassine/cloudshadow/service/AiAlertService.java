package com.yassine.cloudshadow.service;

import com.yassine.cloudshadow.dto.response.AlertResponse;
import com.yassine.cloudshadow.entity.Alert;
import com.yassine.cloudshadow.entity.Metric;
import com.yassine.cloudshadow.entity.Server;
import com.yassine.cloudshadow.enums.Severity;
import com.yassine.cloudshadow.repository.AlertRepository;
import com.yassine.cloudshadow.repository.MetricRepository;
import com.yassine.cloudshadow.repository.ServerRepository;
import com.yassine.cloudshadow.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAlertService {

    private final ServerRepository serverRepository;
    private final MetricRepository metricRepository;
    private final AlertRepository alertRepository;
    private final WebSocketService webSocketService;
    private final RestTemplate restTemplate;

    @Value("${cloudshadow.ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    private static final int METRICS_WINDOW = 30;

    // ─── Run AI analysis every 60 seconds ─────────────────────────────────
    @Scheduled(fixedDelay = 60000)
    public void analyzeAllServers() {
        log.info("🤖 Starting AI analysis for all servers...");

        List<Server> allServers = serverRepository.findAll();

        for (Server server : allServers) {
            try {
                analyzeServer(server);
            } catch (Exception e) {
                log.error("❌ AI analysis failed for server {}: {}",
                        server.getName(), e.getMessage());
            }
        }

        log.info("✅ AI analysis complete for {} servers", allServers.size());
    }

    // ─── Analyze Single Server ────────────────────────────────────────────
    private void analyzeServer(Server server) {

        // Get last 30 metrics
        List<Metric> recentMetrics = metricRepository
                .findLastNByServerId(server.getId(), METRICS_WINDOW);

        if (recentMetrics.size() < 10) {
            return; // Not enough data
        }

        // Build request payload
        Map<String, Object> request = buildAiRequest(server, recentMetrics);

        // Call Python AI service
        Map<String, Object> response = callAiService(request);

        if (response == null) {
            return;
        }

        // Process response
        processAiResponse(server, response);
    }

    // ─── Build Request for Python AI ──────────────────────────────────────
    private Map<String, Object> buildAiRequest(
            Server server,
            List<Metric> metrics) {

        List<Map<String, Object>> metricsData = new ArrayList<>();

        for (Metric m : metrics) {
            Map<String, Object> metricMap = new HashMap<>();
            metricMap.put("cpu", m.getCpu());
            metricMap.put("memory", m.getMemory());
            metricMap.put("disk", m.getDisk());
            metricMap.put("networkIn", m.getNetworkIn() != null ? m.getNetworkIn() : 0.0);
            metricMap.put("networkOut", m.getNetworkOut() != null ? m.getNetworkOut() : 0.0);
            metricMap.put("timestamp", m.getTimestamp().toString());
            metricsData.add(metricMap);
        }

        Map<String, Object> request = new HashMap<>();
        request.put("serverId", server.getId());
        request.put("serverName", server.getName());
        request.put("metrics", metricsData);

        return request;
    }

    // ─── Call Python AI Service ───────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private Map<String, Object> callAiService(Map<String, Object> request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    aiServiceUrl + "/analyze",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            return response.getBody();

        } catch (Exception e) {
            log.error("❌ Failed to call AI service: {}", e.getMessage());
            return null;
        }
    }

    // ─── Process AI Response ──────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private void processAiResponse(Server server, Map<String, Object> response) {

        Map<String, Object> alertMap = (Map<String, Object>) response.get("alert");

        if (alertMap == null || !Boolean.TRUE.equals(alertMap.get("shouldAlert"))) {
            return;
        }

        // ── Safe casting with null checks ────────────────────────────────────
        String type           = (String) alertMap.get("type");
        String severityStr    = (String) alertMap.get("severity");
        String message        = (String) alertMap.get("message");
        String predWindow     = (String) alertMap.get("predictionWindow");
        String action         = (String) alertMap.get("recommendedAction");

        Object confRaw        = alertMap.get("confidenceScore");
        Float confidenceScore = confRaw != null
                ? ((Number) confRaw).floatValue()
                : null;

        // ── Guard: type and message must exist ───────────────────────────────
        if (type == null || message == null) {
            log.warn("AI response missing type or message for server {}",
                    server.getName());
            return;
        }

        // ── Spam prevention ──────────────────────────────────────────────────
        if (alertRepository.existsRecentAiAlert(
                server.getId(), type, LocalDateTime.now().minusHours(1))) {
            log.debug("Skipping duplicate AI alert for {} - {}",
                    server.getName(), type);
            return;
        }

        // ── Build and save alert ─────────────────────────────────────────────
        Severity severity = "CRITICAL".equals(severityStr)
                ? Severity.CRITICAL
                : Severity.WARNING;

        Alert alertEntity = Alert.builder()
                .server(server)
                .type(type)
                .severity(severity)
                .message(message)
                .isAiGenerated(true)
                .confidenceScore(confidenceScore)
                .predictionWindow(predWindow)
                .recommendedAction(action)
                .timestamp(LocalDateTime.now())
                .build();

        alertRepository.save(alertEntity);

        // ── Push to frontend ─────────────────────────────────────────────────
        AlertResponse alertResponse = mapToResponse(alertEntity);
        webSocketService.pushAlert(alertResponse, server.getCompany().getId());

        log.info("🤖 AI Alert: {} on {} [{}] confidence: {}",
                type, server.getName(), severity, confidenceScore);
    }

    // ─── Map to Response ──────────────────────────────────────────────────
    private AlertResponse mapToResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .serverId(alert.getServer().getId())
                .serverName(alert.getServer().getName())
                .type(alert.getType())
                .severity(alert.getSeverity())
                .message(alert.getMessage())
                .timestamp(alert.getTimestamp())
                .isAiGenerated(alert.getIsAiGenerated())
                .confidenceScore(alert.getConfidenceScore())
                .predictionWindow(alert.getPredictionWindow())
                .recommendedAction(alert.getRecommendedAction())
                .build();
    }
}