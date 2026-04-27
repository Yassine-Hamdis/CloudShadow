package com.yassine.cloudshadow.entity;


import com.yassine.cloudshadow.enums.Severity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @Column(nullable = false)
    private String type;          // CPU / Memory / Disk

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;    // WARNING / CRITICAL

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    // ─── NEW: AI fields ──────────────────────────────────────────────────

    // false = threshold alert (AiService)
    // true  = AI pattern alert (AiAlertService)
    @Column(name = "is_ai_generated")
    @Builder.Default
    private Boolean isAiGenerated = false;

    // 0.0 - 1.0 (null for threshold alerts)
    @Column(name = "confidence_score")
    private Float confidenceScore;

    // "25 minutes" / "2 hours" (null for threshold alerts)
    @Column(name = "prediction_window")
    private String predictionWindow;

    // "Scale up server" (null for threshold alerts)
    @Column(name = "recommended_action")
    private String recommendedAction;

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}