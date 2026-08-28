package com.mygaadi.security;

import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.mygaadi.model.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtUtils {
    @Value("${app.jwt.access-token-minutes}")
    private long expTimeMinutes;

    @Value("${app.jwt.secret}")
    private String key;

    private SecretKey secretKey;

    @PostConstruct
    public void myInit() {
        secretKey = Keys.hmacShaKeyFor(key.getBytes());
        log.info("****** in init ***** {} ", secretKey);
    }

    public String generateAccessToken(User user) {
        Date createdAt = new Date();
        long expTimeMs = expTimeMinutes * 60 * 1000;
        Date expAt = new Date(createdAt.getTime() + expTimeMs);
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(createdAt)
                .expiration(expAt)
                .claims(Map.of(
                        "uid", user.getId(),
                        "role", user.getRole().name()
                ))
                .signWith(secretKey)
                .compact();
    }

    public Claims verifyJwt(String jwt) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    public String extractEmail(String token) {
        return verifyJwt(token).getSubject();
    }
}
