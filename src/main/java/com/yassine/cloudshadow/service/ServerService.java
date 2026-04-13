package com.yassine.cloudshadow.service;

import com.yassine.cloudshadow.dto.request.AddServerRequest;
import com.yassine.cloudshadow.dto.response.InstallInstructions;
import com.yassine.cloudshadow.dto.response.ServerResponse;
import com.yassine.cloudshadow.entity.Company;
import com.yassine.cloudshadow.entity.Server;
import com.yassine.cloudshadow.exception.ResourceNotFoundException;
import com.yassine.cloudshadow.repository.CompanyRepository;
import com.yassine.cloudshadow.repository.ServerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServerService {

    private final ServerRepository serverRepository;
    private final CompanyRepository companyRepository;
    private final InstallInstructionsGenerator instructionsGenerator;

    // ── Backend URL (used in install instructions) ─────────────────────────
    @Value("${cloudshadow.backend.public-url:http://localhost:8080}")
    private String backendPublicUrl;

    // ─── Add Server ───────────────────────────────────────────────────────
    @Transactional
    public ServerResponse addServer(AddServerRequest request, Long companyId) {

        // 1. Load company
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Company not found: " + companyId
                        )
                );

        // 2. Check duplicate server name within company
        if (serverRepository.existsByNameAndCompanyId(
                request.getName(), companyId)) {
            throw new IllegalArgumentException(
                    "Server name already exists in your company: "
                            + request.getName()
            );
        }

        // 3. Generate unique token
        String token = generateUniqueToken();

        // 4. Build and save Server
        Server server = Server.builder()
                .company(company)
                .name(request.getName())
                .token(token)
                .build();
        serverRepository.save(server);

        // 5. Generate install instructions
        InstallInstructions instructions = instructionsGenerator.generate(
                token,
                backendPublicUrl,
                server.getName()
        );

        // 6. Return response WITH install instructions
        return mapToResponse(server, instructions);
    }

    // ─── Get All Servers for Company ──────────────────────────────────────
    // NOTE: No install instructions on list — token already saved by admin
    public List<ServerResponse> getServersByCompany(Long companyId) {
        return serverRepository.findAllByCompanyId(companyId)
                .stream()
                .map(server -> mapToResponse(server, null))
                .collect(Collectors.toList());
    }

    // ─── Get Single Server ────────────────────────────────────────────────
    public ServerResponse getServerById(Long serverId, Long companyId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Server not found: " + serverId
                        )
                );

        // Multi-tenant check
        if (!server.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException(
                    "Server not found: " + serverId
            );
        }

        // No instructions on single fetch — token already saved
        return mapToResponse(server, null);
    }

    // ─── Delete Server ────────────────────────────────────────────────────
    @Transactional
    public void deleteServer(Long serverId, Long companyId) {
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Server not found: " + serverId
                        )
                );

        // Multi-tenant check
        if (!server.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException(
                    "Server not found: " + serverId
            );
        }

        serverRepository.delete(server);
    }

    // ─── Regenerate Install Instructions ──────────────────────────────────
    // Admin can call this if they lost the instructions
    public ServerResponse regenerateInstructions(
            Long serverId, Long companyId) {

        Server server = serverRepository.findById(serverId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Server not found: " + serverId
                        )
                );

        // Multi-tenant check
        if (!server.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException(
                    "Server not found: " + serverId
            );
        }

        // Regenerate instructions with existing token
        InstallInstructions instructions = instructionsGenerator.generate(
                server.getToken(),
                backendPublicUrl,
                server.getName()
        );

        return mapToResponse(server, instructions);
    }

    // ─── Generate Unique Token ────────────────────────────────────────────
    private String generateUniqueToken() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "");
        } while (serverRepository.existsByToken(token));
        return token;
    }

    // ─── Map Entity → Response ────────────────────────────────────────────
    private ServerResponse mapToResponse(
            Server server,
            InstallInstructions instructions) {

        return ServerResponse.builder()
                .id(server.getId())
                .name(server.getName())
                .token(server.getToken())
                .companyId(server.getCompany().getId())
                .createdAt(server.getCreatedAt())
                .lastSeen(server.getLastSeen())          // ← ADD THIS LINE
                .installInstructions(instructions)
                .build();
    }
}