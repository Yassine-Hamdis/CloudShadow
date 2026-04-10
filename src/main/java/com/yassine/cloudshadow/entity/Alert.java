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

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}