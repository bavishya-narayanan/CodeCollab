package com.codecollab.security;

import com.codecollab.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    @Value("${codecollab.jwt.secret}")
    private String secret;

    @Value("${codecollab.jwt.expiration-seconds:86400}")
    private long expirationSeconds;

    @Value("${codecollab.jwt.issuer:codecollab}")
    private String issuer;

    @PostConstruct
    public void init() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters long for HS256.");
        }
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(user.getId().toString())
            .issuer(issuer)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(expirationSeconds)))
            .claim("username", user.getUsername())
            .claim("role", user.getRole().name())
            .signWith(signingKey())
            .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser()
            .verifyWith(signingKey())
            .requireIssuer(issuer)
            .build()
            .parseSignedClaims(token);
    }

    public Long extractUserId(String token) {
        return Long.valueOf(parse(token).getPayload().getSubject());
    }

    public String extractUsername(String token) {
        return parse(token).getPayload().get("username", String.class);
    }

    public String extractRole(String token) {
        return parse(token).getPayload().get("role", String.class);
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT: {}", ex.getMessage());
            return false;
        }
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
