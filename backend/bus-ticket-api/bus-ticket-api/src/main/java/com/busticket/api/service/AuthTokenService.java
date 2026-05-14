package com.busticket.api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthTokenService {

    public static final String TYPE_VERIFY_EMAIL = "VERIFY_EMAIL";
    public static final String TYPE_RESET_PASSWORD = "RESET_PASSWORD";

    private final String secret;

    public AuthTokenService(@Value("${app.jwt.secret}") String secret) {
        this.secret = secret;
    }

    public String createToken(Long maTK, String email, String type, int secondsToLive) {
        Instant now = Instant.now();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(maTK))
                .claim("maTK", maTK)
                .claim("email", email)
                .claim("type", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(secondsToLive)))
                .signWith(signingKey())
                .compact();
    }

    public Optional<AuthTokenRecord> findValidToken(String token, String type) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token.trim())
                    .getPayload();

            String tokenType = claims.get("type", String.class);

            if (!type.equals(tokenType)) {
                return Optional.empty();
            }

            Long maTK = extractMaTK(claims);
            String email = claims.get("email", String.class);

            if (maTK == null || email == null || email.isBlank()) {
                return Optional.empty();
            }

            return Optional.of(new AuthTokenRecord(claims.getId(), maTK, email));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public void markUsed(String id) {
        // Stateless JWT links are validated by signature and expiration only.
    }

    private Long extractMaTK(Claims claims) {
        Object maTK = claims.get("maTK");

        if (maTK instanceof Number number) {
            return number.longValue();
        }

        if (maTK instanceof String value && value.matches("\\d+")) {
            return Long.parseLong(value);
        }

        if (claims.getSubject() != null && claims.getSubject().matches("\\d+")) {
            return Long.parseLong(claims.getSubject());
        }

        return null;
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public record AuthTokenRecord(String id, Long maTK, String email) {
    }
}
