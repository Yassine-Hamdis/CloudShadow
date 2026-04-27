package com.yassine.cloudshadow.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "metrics")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @Column(nullable = false)
    private Float cpu;

    @Column(nullable = false)
    private Float memory;

    @Column(nullable = false)
    private Float disk;

    // Network I/O in KB/s
    @Column(name = "network_in")
    private Float networkIn;

    @Column(name = "network_out")
    private Float networkOut;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}