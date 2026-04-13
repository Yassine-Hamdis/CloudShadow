package com.yassine.cloudshadow.service;

import com.yassine.cloudshadow.dto.request.MetricRequest;
import com.yassine.cloudshadow.dto.response.MetricResponse;
import com.yassine.cloudshadow.entity.Metric;
import com.yassine.cloudshadow.entity.Server;
import com.yassine.cloudshadow.exception.ResourceNotFoundException;
import com.yassine.cloudshadow.exception.UnauthorizedException;
import com.yassine.cloudshadow.repository.MetricRepository;
import com.yassine.cloudshadow.repository.ServerRepository;
import com.yassine.cloudshadow.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricService {

    private final MetricRepository metricRepository;
    private final ServerRepository serverRepository;
    private final AlertService alertService;
    private final AiService aiService;
    private final WebSocketService webSocketService;

    // ─── Receive Metrics from Agent ────────────────────────────────────
    @Transactional
    public void receiveMetrics(MetricRequest request) {

        // 1. Validate server token
        Server server = serverRepository.findByToken(request.getToken())
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid server token")
                );

        // 2. Track online/offline transition ───────────────────────────
        // Check if this is the first metric after being offline/new
        // A server is considered "coming online" if:
        // - lastSeen is null (never sent a metric before)
        // - lastSeen is older than 5 minutes (was offline)
        boolean wasOffline = server.getLastSeen() == null ||
                server.getLastSeen()
                        .isBefore(LocalDateTime.now().minusMinutes(5));

        // 3. Update lastSeen timestamp ──────────────────────────────────
        server.setLastSeen(LocalDateTime.now());
        serverRepository.save(server);

        // 4. Build and save Metric
        Metric metric = Metric.builder()
                .server(server)
                .cpu(request.getCpu())
                .memory(request.getMemory())
                .disk(request.getDisk())
                .networkIn(request.getNetworkIn())
                .networkOut(request.getNetworkOut())
                .build();
        metricRepository.save(metric);

        // 5. Map to response
        MetricResponse metricResponse = mapToResponse(metric);

        // 6. Push metric to frontend via WebSocket
        Long companyId = server.getCompany().getId();
        webSocketService.pushMetric(metricResponse, companyId);

        // 7. Push SERVER_ONLINE if server was previously offline ────────
        if (wasOffline) {
            webSocketService.pushServerOnline(
                    server.getId(),
                    server.getName(),
                    companyId,
                    server.getLastSeen()
            );
        }

        // 8. Send to AI for analysis
        AiService.AiOutput aiOutput = aiService.analyze(metric);

        // 9. If anomaly detected → create alert + push via WebSocket
        if (aiOutput.isAnomaly()) {
            alertService.createAlert(server, aiOutput);
        }
    }

    // ─── Get All Metrics for Company ───────────────────────────────────
    public List<MetricResponse> getMetricsByCompany(Long companyId) {
        return metricRepository.findAllByCompanyId(companyId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Metrics for a Specific Server ─────────────────────────────
    public List<MetricResponse> getMetricsByServer(
            Long serverId, Long companyId) {

        Server server = serverRepository.findById(serverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Server not found: " + serverId)
                );

        if (!server.getCompany().getId().equals(companyId)) {
            throw new UnauthorizedException(
                    "Access denied to server: " + serverId
            );
        }

        return metricRepository.findAllByServerId(serverId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Metrics by Time Range ─────────────────────────────────────
    public List<MetricResponse> getMetricsByCompanyAndTimeRange(
            Long companyId,
            LocalDateTime from,
            LocalDateTime to) {

        return metricRepository
                .findAllByCompanyIdAndTimestampBetween(companyId, from, to)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Latest Metric per Server ──────────────────────────────────
    public MetricResponse getLatestMetricByServer(
            Long serverId, Long companyId) {

        Server server = serverRepository.findById(serverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Server not found: " + serverId)
                );

        if (!server.getCompany().getId().equals(companyId)) {
            throw new UnauthorizedException(
                    "Access denied to server: " + serverId
            );
        }

        Metric metric = metricRepository.findLatestByServerId(serverId);
        if (metric == null) {
            throw new ResourceNotFoundException(
                    "No metrics found for server: " + serverId
            );
        }

        return mapToResponse(metric);
    }

    // ─── Map Entity → Response ─────────────────────────────────────────
    private MetricResponse mapToResponse(Metric metric) {
        return MetricResponse.builder()
                .id(metric.getId())
                .serverId(metric.getServer().getId())
                .serverName(metric.getServer().getName())
                .cpu(metric.getCpu())
                .memory(metric.getMemory())
                .disk(metric.getDisk())
                .networkIn(metric.getNetworkIn())
                .networkOut(metric.getNetworkOut())
                .timestamp(metric.getTimestamp())
                .build();
    }
}