package com.yassine.cloudshadow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerResponse {

    private Long id;
    private String name;
    private String token;
    private Long companyId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private LocalDateTime createdAt;

    // ─── NEW: last time agent sent a metric ────────────────────────────
    // null if server never sent a metric yet
    // frontend uses this to show "last seen X minutes ago"
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private LocalDateTime lastSeen;

    // ─── Install instructions (only returned on server creation) ───────
    // null when fetching existing servers (GET /api/servers)
    // populated only on POST /api/servers
    private InstallInstructions installInstructions;
}