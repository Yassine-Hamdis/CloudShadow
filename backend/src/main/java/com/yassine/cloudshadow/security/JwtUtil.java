package com.yassine.cloudshadow.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration; // in milliseconds

    // ─── Generate Key from secret ───────────────────────────────────────
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ─── Generate JWT Token ─────────────────────────────────────────────
    public String generateToken(String email, String role, Long companyId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("companyId", companyId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ─── Extract Email (subject) ─────────────────────────────────────────
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ─── Extract Role ────────────────────────────────────────────────────
    public String extractRole(String token) {
        return (String) extractAllClaims(token).get("role");
    }

    // ─── Extract Company ID ───────────────────────────────────────────────
    public Long extractCompanyId(String token) {
        return ((Number) extractAllClaims(token).get("companyId")).longValue();
    }

    // ─── Validate Token ───────────────────────────────────────────────────
    public boolean isTokenValid(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return extractedEmail.equals(email) && !isTokenExpired(token);
    }

    // ─── Check Expiration ─────────────────────────────────────────────────
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    // ─── Extract All Claims ───────────────────────────────────────────────
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}