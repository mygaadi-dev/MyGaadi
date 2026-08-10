package com.mygaadi.payment.security;

import com.mygaadi.payment.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        String secret = properties.getSecret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes and match the main MyGaadi application");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public JwtPrincipal parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object uidClaim = claims.get("uid");
        String role = claims.get("role", String.class);
        if (uidClaim == null || role == null || claims.getSubject() == null) {
            throw new IllegalArgumentException("Required JWT claims are missing");
        }
        Long uid = Long.valueOf(String.valueOf(uidClaim));

        return JwtPrincipal.builder()
                .userId(uid)
                .email(claims.getSubject())
                .role(UserRole.valueOf(role))
                .build();
    }
}
