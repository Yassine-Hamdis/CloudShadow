package com.yassine.cloudshadow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddServerRequest {

    @NotBlank(message = "Server name is required")
    private String name;
}