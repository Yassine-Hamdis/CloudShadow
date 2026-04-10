package com.yassine.cloudshadow.controller;


import com.yassine.cloudshadow.dto.request.AddServerRequest;
import com.yassine.cloudshadow.dto.response.ApiResponse;
import com.yassine.cloudshadow.dto.response.ServerResponse;
import com.yassine.cloudshadow.security.JwtUtil;
import com.yassine.cloudshadow.service.ServerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servers")
@RequiredArgsConstructor
public class ServerController {

    private final ServerService serverService;
    private final JwtUtil jwtUtil;

    // ─── POST /api/servers ────────────────────────────────────────────────
    // ADMIN only — Add new server
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServerResponse>> addServer(
            @Valid @RequestBody AddServerRequest request,
            @RequestHeader("Authorization") String authHeader) {

        Long companyId = extractCompanyId(authHeader);
        ServerResponse response = serverService.addServer(request, companyId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Server added successfully. Save your token!",
                        response
                ));
    }

    // ─── GET /api/servers/{id}/instructions
    // ───────────────────────────────────
    // ADMIN only — Regenerate install instructions if admin lost them
    @GetMapping("/{id}/instructions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServerResponse>> regenerateInstructions(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long companyId = extractCompanyId(authHeader);
        ServerResponse response =
                serverService.regenerateInstructions(id, companyId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Install instructions regenerated successfully",
                        response
                )
        );
    }

    // ─── GET /api/servers ─────────────────────────────────────────────────
    // ADMIN only — List all servers for company
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ServerResponse>>> getServers(
            @RequestHeader("Authorization") String authHeader) {

        Long companyId = extractCompanyId(authHeader);
        List<ServerResponse> servers =
                serverService.getServersByCompany(companyId);

        return ResponseEntity.ok(
                ApiResponse.success("Servers fetched successfully", servers)
        );
    }

    // ─── GET /api/servers/{id} ────────────────────────────────────────────
    // ADMIN only — Get single server
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ServerResponse>> getServerById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long companyId = extractCompanyId(authHeader);
        ServerResponse response = serverService.getServerById(id, companyId);

        return ResponseEntity.ok(
                ApiResponse.success("Server fetched successfully", response)
        );
    }

    // ─── DELETE /api/servers/{id} ─────────────────────────────────────────
    // ADMIN only — Delete a server
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteServer(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long companyId = extractCompanyId(authHeader);
        serverService.deleteServer(id, companyId);

        return ResponseEntity.ok(
                ApiResponse.success("Server deleted successfully", null)
        );
    }

    // ─── Extract companyId from JWT ───────────────────────────────────────
    private Long extractCompanyId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractCompanyId(token);
    }
}