package com.mygaadi.payment.security;

import com.mygaadi.payment.enums.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final String SECRET = "test-secret-that-is-long-enough-for-hmac-sha-256-testing";

    @Test
    void parsesValidMyGaadiJwt() {
        JwtService service = service(SECRET);
        String token = Jwts.builder()
                .subject("buyer@mygaadi.com")
                .claim("uid", 21L)
                .claim("role", "BUYER")
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(300)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        JwtPrincipal principal = service.parse(token);

        assertEquals(21L, principal.getUserId());
        assertEquals("buyer@mygaadi.com", principal.getEmail());
        assertEquals(UserRole.BUYER, principal.getRole());
    }

    @Test
    void rejectsExpiredJwt() {
        JwtService service = service(SECRET);
        String token = Jwts.builder()
                .subject("buyer@mygaadi.com")
                .claim("uid", 21L)
                .claim("role", "BUYER")
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThrows(RuntimeException.class, () -> service.parse(token));
    }

    @Test
    void rejectsTokenSignedWithDifferentSecret() {
        JwtService service = service(SECRET);
        String otherSecret = "another-test-secret-that-is-long-enough-for-hmac-sha-256";
        String token = Jwts.builder()
                .subject("buyer@mygaadi.com")
                .claim("uid", 21L)
                .claim("role", "BUYER")
                .expiration(Date.from(Instant.now().plusSeconds(300)))
                .signWith(Keys.hmacShaKeyFor(otherSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThrows(RuntimeException.class, () -> service.parse(token));
    }

    private JwtService service(String secret) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);
        return new JwtService(properties);
    }
}
