package com.yassine.cloudshadow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstallInstructions {

    // ─── Normal Server (Python Agent) ─────────────────────────────────────
    private String normalServer;

    // ─── Docker (Phase 2) ─────────────────────────────────────────────────
    private String docker;

    // ─── Kubernetes (Phase 3) ─────────────────────────────────────────────
    private String kubernetes;

    // ─── Environment Variables needed ─────────────────────────────────────
    private String envVariables;

    // ─── Token reminder ───────────────────────────────────────────────────
    private String tokenReminder;
}