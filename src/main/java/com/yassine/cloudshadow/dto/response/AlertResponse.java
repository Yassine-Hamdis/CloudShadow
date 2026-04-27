package com.yassine.cloudshadow.dto.response;

import com.yassine.cloudshadow.enums.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {

    private Long id;
    private Long serverId;
    private String serverName;      // Included for dashboard display
    private String type;            // CPU / Memory / Disk
    private Severity severity;      // WARNING / CRITICAL
    private String message;
    private LocalDateTime timestamp;

    // ─── NEW: AI fields ──────────────────────────────────────────────────
    // null for threshold alerts, populated for AI alerts
    private Boolean isAiGenerated;
    private Float confidenceScore;
    private String predictionWindow;
    private String recommendedAction;
}