package com.yassine.cloudshadow.controller;


import com.yassine.cloudshadow.dto.request.LoginRequest;
import com.yassine.cloudshadow.dto.request.RegisterRequest;
import com.yassine.cloudshadow.dto.response.ApiResponse;
import com.yassine.cloudshadow.dto.response.AuthResponse;
import com.yassine.cloudshadow.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ─── POST /api/auth/register ──────────────────────────────────────────
    // Public — Company + Admin signup
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Company and admin registered successfully",
                        response
                ));
    }

    // ─── POST /api/auth/login ─────────────────────────────────────────────
    // Public — Login and get JWT
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response)
        );
    }
}