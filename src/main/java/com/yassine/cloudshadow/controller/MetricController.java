package com.yassine.cloudshadow.controller;

import com.yassine.cloudshadow.dto.request.MetricRequest;
import com.yassine.cloudshadow.dto.response.ApiResponse;
import com.yassine.cloudshadow.dto.response.MetricResponse;
import com.yassine.cloudshadow.security.JwtUtil;
import com.yassine.cloudshadow.service.MetricService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricController {

    private final MetricService metricService;
    private final JwtUtil jwtUtil;

    // ─── POST /api/metrics ────────────────────────────────────────────────
    // Public — Agent sends metrics using server token
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> receiveMetrics(
            @Valid @RequestBody MetricRequest request) {

        metricService.receiveMetrics(request);

        return ResponseEntity.ok(
                ApiResponse.success("Metrics received and processed", null)
        );
    }

    // ─── GET /api/metrics ─────────────────────────────────────────────────
    // Authenticated — Get all metrics for company
    @GetMapping
    public ResponseEntity<ApiResponse<List<MetricResponse>>> getMetrics(
            @RequestHeader("Authorization") String authHeader) {

        Long companyId = extractCompanyId(authHeader);
        List<MetricResponse> metrics =
                metricService.getMetricsByCompany(companyId);

        return ResponseEntity.ok(
                ApiResponse.success("Metrics fetched successfully", metrics)
        );
    }

    // ─── GET /api/metrics/server/{serverId} ───────────────────────────────
    // Authenticated — Get metrics for a specific server
    @GetMapping("/server/{serverId}")
    public ResponseEntity<ApiResponse<List<MetricResponse>>> getMetricsByServer(
            @PathVariable Long serverId,
            @RequestHeader("Authorization") String authHeader) {

        Long companyId = extractCompanyId(authHeader);
        List<MetricResponse> metrics =
                metricService.getMetricsByServer(serverId, companyId);

        return ResponseEntity.ok(
                ApiResponse.success("Server metrics fetched successfully", metrics)
        );
    }

    // ─── GET /api/metrics/server/{serverId}/latest ────────────────────────
    // Authenticated — Get latest metric for a server (dashboard summary card)
    @GetMapping("/server/{serverId}/latest")
    public ResponseEntity<ApiResponse<MetricResponse>> getLatestMetric(
            @PathVariable Long serverId,
            @RequestHeader("Authorization") String authHeader) {

        Long companyId = extractCompanyId(authHeader);
        MetricResponse metric =
                metricService.getLatestMetricByServer(serverId, companyId);

        return ResponseEntity.ok(
                ApiResponse.success("Latest metric fetched successfully", metric)
        );
    }

    // ─── GET /api/metrics/range ───────────────────────────────────────────
    // Authenticated — Get metrics within time range (for charts/trends)
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<MetricResponse>>> getMetricsByRange(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to) {

        Long companyId = extractCompanyId(authHeader);
        List<MetricResponse> metrics =
                metricService.getMetricsByCompanyAndTimeRange(
                        companyId, from, to
                );

        return ResponseEntity.ok(
                ApiResponse.success("Metrics range fetched successfully", metrics)
        );
    }

    // ─── Extract companyId from JWT ───────────────────────────────────────
    private Long extractCompanyId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractCompanyId(token);
    }
}