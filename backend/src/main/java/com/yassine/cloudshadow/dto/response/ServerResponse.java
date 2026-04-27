package com.yassine.cloudshadow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerResponse {

    private Long id;
    private String name;
    private String token;
    private Long companyId;
    private LocalDateTime createdAt;

    // ─── NEW: last time agent sent a metric ────────────────────────────
    // null if server never sent a metric yet
    // frontend uses this to show "last seen X minutes ago"
    private LocalDateTime lastSeen;

    // ─── Install instructions (only returned on server creation) ───────
    // null when fetching existing servers (GET /api/servers)
    // populated only on POST /api/servers
    private InstallInstructions installInstructions;
}