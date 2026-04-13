package com.yassine.cloudshadow.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MetricRequest {

    @NotBlank(message = "Server token is required")
    private String token;

    @NotNull(message = "CPU value is required")
    @Min(value = 0, message = "CPU must be between 0 and 100")
    @Max(value = 100, message = "CPU must be between 0 and 100")
    private Float cpu;

    @NotNull(message = "Memory value is required")
    @Min(value = 0, message = "Memory must be between 0 and 100")
    @Max(value = 100, message = "Memory must be between 0 and 100")
    private Float memory;

    @NotNull(message = "Disk value is required")
    @Min(value = 0, message = "Disk must be between 0 and 100")
    @Max(value = 100, message = "Disk must be between 0 and 100")
    private Float disk;

    // Network — optional (KB/s)
    @JsonProperty("network_in")
    private Float networkIn  = 0.0f;

    @JsonProperty("network_out")
    private Float networkOut = 0.0f;
}