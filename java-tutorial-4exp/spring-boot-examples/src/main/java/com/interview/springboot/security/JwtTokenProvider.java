package com.interview.springboot.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * JWT Token Provider — generation, validation, claims extraction.
 *
 * Interview points:
 * - HMAC-SHA256 for single-service (use RSA for multi-service)
 * - Short-lived tokens (15 min) + refresh token pattern
 * - Claims contain userId + roles (avoid sensitive data)
 * - Never log or expose the secret key
 */
@Component
public class JwtTokenProvider {

    // In production: load from environment variable or Vault
    private static final String SECRET = "my-super-secret-key-that-is-at-least-256-bits-long-for-hs256";
    private static final long EXPIRATION_MS = 15 * 60 * 1000; // 15 minutes

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /**
     * Generate JWT with userId and roles.
     */
    public String generateToken(String userId, List<String> roles) {
        return Jwts.builder()
            .subject(userId)
            .claims(Map.of("roles", roles))
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
            .signWith(key)
            .compact();
    }

    /**
     * Validate token — checks signature and expiry.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false; // expired, tampered, or malformed
        }
    }

    /**
     * Extract username (subject) from token.
     */
    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extract roles from token claims.
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        return (List<String>) getClaims(token).get("roles");
    }

    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload();
    }
}
