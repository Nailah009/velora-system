package com.velora.inventory.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Service
public class JwtService {
    @Value("${app.jwt.secret:velora-super-secret-key-for-jwt-hs256-minimum-32-bytes}")
    private String secret;

    private Key key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
