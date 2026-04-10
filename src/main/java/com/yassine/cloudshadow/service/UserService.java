package com.yassine.cloudshadow.service;

import com.yassine.cloudshadow.dto.request.CreateUserRequest;
import com.yassine.cloudshadow.dto.response.UserResponse;
import com.yassine.cloudshadow.entity.Company;
import com.yassine.cloudshadow.entity.User;
import com.yassine.cloudshadow.exception.ResourceNotFoundException;
import com.yassine.cloudshadow.repository.CompanyRepository;
import com.yassine.cloudshadow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── Admin Creates a New User ─────────────────────────────────────────
    @Transactional
    public UserResponse createUser(
            CreateUserRequest request, Long companyId) {

        // 1. Check email not taken
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already in use: " + request.getEmail()
            );
        }

        // 2. Load company
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Company not found: " + companyId
                        )
                );

        // 3. Build and save User
        User user = User.builder()
                .company(company)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        userRepository.save(user);

        return mapToResponse(user);
    }

    // ─── Get All Users in Company ─────────────────────────────────────────
    public List<UserResponse> getUsersByCompany(Long companyId) {
        return userRepository.findAllByCompanyId(companyId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Single User ──────────────────────────────────────────────────
    public UserResponse getUserById(Long userId, Long companyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + userId
                        )
                );

        // Multi-tenant check
        if (!user.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException(
                    "User not found: " + userId
            );
        }

        return mapToResponse(user);
    }

    // ─── Delete User ──────────────────────────────────────────────────────
    @Transactional
    public void deleteUser(Long userId, Long companyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + userId
                        )
                );

        // Multi-tenant check
        if (!user.getCompany().getId().equals(companyId)) {
            throw new ResourceNotFoundException(
                    "User not found: " + userId
            );
        }

        userRepository.delete(user);
    }

    // ─── Map Entity → Response ────────────────────────────────────────────
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .companyId(user.getCompany().getId())
                .companyName(user.getCompany().getName())
                .build();
    }
}