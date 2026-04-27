package com.yassine.cloudshadow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "servers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // ─── NEW: tracks last metric received time ─────────────────────────
    // Updated every time agent sends a metric
    // Used by scheduler to detect offline servers
    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @OneToMany(mappedBy = "server",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<Metric> metrics;

    @OneToMany(mappedBy = "server",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<Alert> alerts;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}