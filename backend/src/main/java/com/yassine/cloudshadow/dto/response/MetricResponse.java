package com.yassine.cloudshadow.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricResponse {

    private Long id;
    private Long serverId;
    private String serverName;
    private Float cpu;
    private Float memory;
    private Float disk;
    private Float networkIn;     // KB/s
    private Float networkOut;    // KB/s
    private LocalDateTime timestamp;
}