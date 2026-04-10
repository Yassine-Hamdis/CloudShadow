package com.yassine.cloudshadow.controller;


import com.yassine.cloudshadow.dto.response.AlertResponse;
import com.yassine.cloudshadow.dto.response.ApiResponse;
import com.yassine.cloudshadow.enums.Severity;
import com.yassine.cloudshadow.security.JwtUtil;
import com.yassine.cloudshadow.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final JwtUtil jwtUtil;

    // ─── GET /api/alerts ──────────────────────────────────────────────────
    // Authenticated — Get all alerts for company
    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAlerts(
            @RequestHeader("Authorization") String authHeader) {

        Long companyId = extractCompanyId(authHeader);
        List<AlertResponse> alerts =
                alertService.getAlertsByCompany(companyId);

        return ResponseEntity.ok(
                ApiResponse.success("Alerts fetched successfully", alerts)
        );
    }

    // ─── GET /api/alerts/server/{serverId} ────────────────────────────────
    // Authenticated — Get alerts for a specific server
    @GetMapping("/server/{serverId}")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAlertsByServer(
            @PathVariable Long serverId,
            @RequestHeader("Authorization") String authHeader) {

        Long companyId = extractCompanyId(authHeader);
        List<AlertResponse> alerts =
                alertService.getAlertsByServer(serverId, companyId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Server alerts fetched successfully", alerts
                )
        );
    }

    // ─── GET /api/alerts/severity/{severity} ──────────────────────────────
    // Authenticated — Filter alerts by severity (WARNING / CRITICAL)
    @GetMapping("/severity/{severity}")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAlertsBySeverity(
            @PathVariable Severity severity,
            @RequestHeader("Authorization") String authHeader) {

        Long companyId = extractCompanyId(authHeader);
        List<AlertResponse> alerts =
                alertService.getAlertsBySeverity(companyId, severity);

        return ResponseEntity.ok(
                ApiResponse.success(
                        severity + " alerts fetched successfully", alerts
                )
        );
    }

    // ─── GET /api/alerts/type/{type} ──────────────────────────────────────
    // Authenticated — Filter alerts by type (CPU / Memory / Disk)
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<AlertResponse>>> getAlertsByType(
            @PathVariable String type,
            @RequestHeader("Authorization") String authHeader) {

        Long companyId = extractCompanyId(authHeader);
        List<AlertResponse> alerts =
                alertService.getAlertsByType(companyId, type);

        return ResponseEntity.ok(
                ApiResponse.success(
                        type + " alerts fetched successfully", alerts
                )
        );
    }

    // ─── GET /api/alerts/critical/count ───────────────────────────────────
    // Authenticated — Count critical alerts (dashboard badge)
    @GetMapping("/critical/count")
    public ResponseEntity<ApiResponse<Long>> countCriticalAlerts(
            @RequestHeader("Authorization") String authHeader) {

        Long companyId = extractCompanyId(authHeader);
        long count = alertService.countCriticalAlerts(companyId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Critical alert count fetched successfully", count
                )
        );
    }

    // ─── Extract companyId from JWT ───────────────────────────────────────
    private Long extractCompanyId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractCompanyId(token);
    }
}