package com.yassine.cloudshadow.config;


import com.yassine.cloudshadow.security.JwtAuthFilter;
import com.yassine.cloudshadow.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    // ─── Security Filter Chain ────────────────────────────────────────────
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        // ── Public endpoints ──
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/metrics",
                                "/ws/**",              // ← WebSocket handshake
                                "/ws/info/**"          // ← SockJS info endpoint
                        ).permitAll()

                        // ── Admin only ──
                        .requestMatchers(
                                "/api/servers/**",
                                "/api/users/**"
                        ).hasRole("ADMIN")

                        // ── Any authenticated user ──
                        .anyRequest().authenticated()
                )

                // ── Stateless session (JWT) ──
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ── Auth provider ──
                .authenticationProvider(authenticationProvider())

                // ── Add JWT filter before Spring's auth filter ──
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    // ─── Password Encoder (BCrypt) ────────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ─── Authentication Provider ──────────────────────────────────────────
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // In Spring Security 6 the no-arg constructor and setUserDetailsService(...) were removed.
        // Pass the UserDetailsService into the constructor instead.
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ─── Authentication Manager ───────────────────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}