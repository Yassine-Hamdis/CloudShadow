package com.yassine.cloudshadow.service;


import com.yassine.cloudshadow.entity.Metric;
import com.yassine.cloudshadow.enums.Severity;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    // ─── AI Output DTO (inner class) ──────────────────────────────────────
    @Data
    @Builder
    public static class AiOutput {
        private boolean isAnomaly;
        private String type;        // CPU / Memory / Disk
        private String message;
        private Severity severity;  // WARNING / CRITICAL
    }

    // ─── Main Analysis Method ─────────────────────────────────────────────
    public AiOutput analyze(Metric metric) {

        // ── CPU Anomaly Detection ──────────────────────────────────────────
        if (metric.getCpu() >= 90) {
            return AiOutput.builder()
                    .isAnomaly(true)
                    .type("CPU")
                    .message("CRITICAL: CPU usage at " + metric.getCpu()
                            + "%. Overload expected in 15 minutes.")
                    .severity(Severity.CRITICAL)
                    .build();
        }

        if (metric.getCpu() >= 75) {
            return AiOutput.builder()
                    .isAnomaly(true)
                    .type("CPU")
                    .message("WARNING: CPU usage at " + metric.getCpu()
                            + "%. Monitor closely.")
                    .severity(Severity.WARNING)
                    .build();
        }

        // ── Memory Anomaly Detection ───────────────────────────────────────
        if (metric.getMemory() >= 90) {
            return AiOutput.builder()
                    .isAnomaly(true)
                    .type("Memory")
                    .message("CRITICAL: Memory usage at " + metric.getMemory()
                            + "%. System may crash soon.")
                    .severity(Severity.CRITICAL)
                    .build();
        }

        if (metric.getMemory() >= 75) {
            return AiOutput.builder()
                    .isAnomaly(true)
                    .type("Memory")
                    .message("WARNING: Memory usage at " + metric.getMemory()
                            + "%. Consider scaling.")
                    .severity(Severity.WARNING)
                    .build();
        }

        // ── Disk Anomaly Detection ─────────────────────────────────────────
        if (metric.getDisk() >= 90) {
            return AiOutput.builder()
                    .isAnomaly(true)
                    .type("Disk")
                    .message("CRITICAL: Disk usage at " + metric.getDisk()
                            + "%. Disk full in less than 1 hour.")
                    .severity(Severity.CRITICAL)
                    .build();
        }

        if (metric.getDisk() >= 75) {
            return AiOutput.builder()
                    .isAnomaly(true)
                    .type("Disk")
                    .message("WARNING: Disk usage at " + metric.getDisk()
                            + "%. Clean up or expand storage.")
                    .severity(Severity.WARNING)
                    .build();
        }

        // ── No Anomaly ────────────────────────────────────────────────────
        return AiOutput.builder()
                .isAnomaly(false)
                .type(null)
                .message("All metrics are normal.")
                .severity(null)
                .build();
    }
}