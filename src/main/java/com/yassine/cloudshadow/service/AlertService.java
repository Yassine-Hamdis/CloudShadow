package com.yassine.cloudshadow.service;


import com.yassine.cloudshadow.dto.response.AlertResponse;
import com.yassine.cloudshadow.entity.Alert;
import com.yassine.cloudshadow.entity.Server;
import com.yassine.cloudshadow.enums.Severity;
import com.yassine.cloudshadow.exception.ResourceNotFoundException;
import com.yassine.cloudshadow.exception.UnauthorizedException;
import com.yassine.cloudshadow.repository.AlertRepository;
import com.yassine.cloudshadow.repository.ServerRepository;
import com.yassine.cloudshadow.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final ServerRepository serverRepository;
    private final WebSocketService webSocketService;  // ← NEW

    // ─── Create Alert + Push via WebSocket ────────────────────────────────
    @Transactional
    public void createAlert(Server server, AiService.AiOutput aiOutput) {

        // 1. Save alert to DB
        Alert alert = Alert.builder()
                .server(server)
                .type(aiOutput.getType())
                .severity(aiOutput.getSeverity())
                .message(aiOutput.getMessage())
                .build();
        alertRepository.save(alert);

        // 2. Map to response
        AlertResponse alertResponse = mapToResponse(alert);

        // 3. Push alert to frontend via WebSocket ── NEW
        Long companyId = server.getCompany().getId();
        webSocketService.pushAlert(alertResponse, companyId);

        log.warn(
                "🚨 Alert created → Server: {} | Type: {} | Severity: {}",
                server.getName(),
                aiOutput.getType(),
                aiOutput.getSeverity()
        );
    }

    // ─── Get All Alerts for Company ───────────────────────────────────────
    public List<AlertResponse> getAlertsByCompany(Long companyId) {
        return alertRepository.findAllByCompanyId(companyId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Alerts for Specific Server ──────────────────────────────────
    public List<AlertResponse> getAlertsByServer(
            Long serverId, Long companyId) {

        Server server = serverRepository.findById(serverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Server not found: " + serverId
                        )
                );

        if (!server.getCompany().getId().equals(companyId)) {
            throw new UnauthorizedException(
                    "Access denied to server: " + serverId
            );
        }

        return alertRepository.findAllByServerId(serverId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Alerts by Severity ───────────────────────────────────────────
    public List<AlertResponse> getAlertsBySeverity(
            Long companyId, Severity severity) {

        return alertRepository
                .findAllByCompanyIdAndSeverity(companyId, severity)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Alerts by Type ───────────────────────────────────────────────
    public List<AlertResponse> getAlertsByType(
            Long companyId, String type) {

        return alertRepository
                .findAllByCompanyIdAndType(companyId, type)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Count Critical Alerts ────────────────────────────────────────────
    public long countCriticalAlerts(Long companyId) {
        return alertRepository.countCriticalAlertsByCompanyId(companyId);
    }

    // ─── Map Entity → Response ────────────────────────────────────────────
    private AlertResponse mapToResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .serverId(alert.getServer().getId())
                .serverName(alert.getServer().getName())
                .type(alert.getType())
                .severity(alert.getSeverity())
                .message(alert.getMessage())
                .timestamp(alert.getTimestamp())
                // ── NEW: AI fields (null for threshold alerts) ────────────
                .isAiGenerated(alert.getIsAiGenerated())
                .confidenceScore(alert.getConfidenceScore())
                .predictionWindow(alert.getPredictionWindow())
                .recommendedAction(alert.getRecommendedAction())
                .build();
    }
}