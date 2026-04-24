package com.eande.store.auth_service.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.algorithm:HS512}")
    private String algorithm;

    // Enforce minimum 512-bit HMAC key
    private static final int MIN_HMAC_KEY_BITS = 512;

    public SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);

        // Validate key strength
        if (keyBytes.length * 8 < MIN_HMAC_KEY_BITS) {
            throw new IllegalStateException(
                    "JWT secret must be at least " + MIN_HMAC_KEY_BITS + " bits " +
                            "(current: " + (keyBytes.length * 8) + " bits)"
            );
        }

        // Explicitly use HS512 (not HS256 which is weaker)
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                // EXPLICITLY require HS512 - prevents algorithm confusion
                .require("alg", "HS512")
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
