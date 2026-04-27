package com.yassine.cloudshadow.service;

import com.yassine.cloudshadow.dto.request.LoginRequest;
import com.yassine.cloudshadow.dto.request.RegisterRequest;
import com.yassine.cloudshadow.dto.response.AuthResponse;
import com.yassine.cloudshadow.entity.Company;
import com.yassine.cloudshadow.entity.User;
import com.yassine.cloudshadow.enums.Role;
import com.yassine.cloudshadow.exception.ResourceNotFoundException;
import com.yassine.cloudshadow.repository.CompanyRepository;
import com.yassine.cloudshadow.repository.UserRepository;
import com.yassine.cloudshadow.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // ─── Register Company + Admin User ────────────────────────────────────
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // 1. Check company name not taken
        if (companyRepository.existsByName(request.getCompanyName())) {
            throw new IllegalArgumentException(
                    "Company name already exists: " + request.getCompanyName()
            );
        }

        // 2. Check email not taken
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already in use: " + request.getEmail()
            );
        }

        // 3. Create and save Company
        Company company = Company.builder()
                .name(request.getCompanyName())
                .build();
        companyRepository.save(company);

        // 4. Create and save Admin User
        User admin = User.builder()
                .company(company)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);

        // 5. Generate JWT
        String token = jwtUtil.generateToken(
                admin.getEmail(),
                admin.getRole().name(),
                company.getId()
        );

        // 6. Return response
        return AuthResponse.builder()
                .token(token)
                .email(admin.getEmail())
                .role(admin.getRole().name())
                .companyId(company.getId())
                .companyName(company.getName())
                .build();
    }

    // ─── Login ────────────────────────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {

        // 1. Authenticate credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Load user from DB
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + request.getEmail()
                        )
                );

        // 3. Generate JWT with companyId + role claims
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name(),
                user.getCompany().getId()
        );

        // 4. Return response
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole().name())
                .companyId(user.getCompany().getId())
                .companyName(user.getCompany().getName())
                .build();
    }
}