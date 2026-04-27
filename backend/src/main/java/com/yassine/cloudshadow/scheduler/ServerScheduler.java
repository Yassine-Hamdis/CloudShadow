package com.yassine.cloudshadow.scheduler;

import com.yassine.cloudshadow.entity.Server;
import com.yassine.cloudshadow.repository.ServerRepository;
import com.yassine.cloudshadow.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServerScheduler {

    private final ServerRepository serverRepository;
    private final WebSocketService webSocketService;

    // ── Offline threshold: 5 minutes ───────────────────────────────────
    // If a server hasn't sent a metric in 5 minutes → mark OFFLINE
    private static final int OFFLINE_THRESHOLD_MINUTES = 5;

    /**
     * Runs every 60 seconds
     * Checks all servers and pushes OFFLINE status
     * for servers that stopped sending metrics
     */
    @Scheduled(fixedDelay = 60000)
    public void checkOfflineServers() {

        LocalDateTime threshold = LocalDateTime.now()
                .minusMinutes(OFFLINE_THRESHOLD_MINUTES);

        // Get all servers across all companies
        List<Server> allServers = serverRepository.findAll();

        for (Server server : allServers) {

            // Skip servers that never sent a metric
            if (server.getLastSeen() == null) {
                continue;
            }

            // If lastSeen is older than threshold → push OFFLINE
            if (server.getLastSeen().isBefore(threshold)) {

                Long companyId = server.getCompany().getId();

                webSocketService.pushServerOffline(
                        server.getId(),
                        server.getName(),
                        companyId,
                        server.getLastSeen()
                );

                log.warn(
                        "🔴 Server offline detected → {} | lastSeen: {}",
                        server.getName(),
                        server.getLastSeen()
                );
            }
        }
    }
}