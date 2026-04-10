package com.yassine.cloudshadow.controller;

import com.yassine.cloudshadow.dto.request.CreateUserRequest;
import com.yassine.cloudshadow.dto.response.ApiResponse;
import com.yassine.cloudshadow.dto.response.UserResponse;
import com.yassine.cloudshadow.security.JwtUtil;
import com.yassine.cloudshadow.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    // ─── POST /api/users ──────────────────────────────────────────────────
    // ADMIN only — Create new user in company
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @RequestHeader("Authorization") String authHeader) {

        Long companyId = extractCompanyId(authHeader);
        UserResponse response = userService.createUser(request, companyId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "User created successfully", response
                ));
    }

    // ─── GET /api/users ───────────────────────────────────────────────────
    // ADMIN only — List all users in company
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsers(
            @RequestHeader("Authorization") String authHeader) {

        Long companyId = extractCompanyId(authHeader);
        List<UserResponse> users = userService.getUsersByCompany(companyId);

        return ResponseEntity.ok(
                ApiResponse.success("Users fetched successfully", users)
        );
    }

    // ─── GET /api/users/{id} ──────────────────────────────────────────────
    // ADMIN only — Get single user
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long companyId = extractCompanyId(authHeader);
        UserResponse response = userService.getUserById(id, companyId);

        return ResponseEntity.ok(
                ApiResponse.success("User fetched successfully", response)
        );
    }

    // ─── DELETE /api/users/{id} ───────────────────────────────────────────
    // ADMIN only — Delete user from company
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long companyId = extractCompanyId(authHeader);
        userService.deleteUser(id, companyId);

        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully", null)
        );
    }

    // ─── Extract companyId from JWT ───────────────────────────────────────
    private Long extractCompanyId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractCompanyId(token);
    }
}