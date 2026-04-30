package com.yassine.cloudshadow.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private LocalDateTime timestamp;
}