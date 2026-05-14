package com.busticket.api.security;

import com.busticket.api.entity.TaiKhoan;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long expirationMs;

    public String generateToken(TaiKhoan taiKhoan) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(String.valueOf(taiKhoan.getMaTK()))
                .claim("maTK", taiKhoan.getMaTK())
                .claim("tenDangNhap", taiKhoan.getTenDangNhap())
                .claim("quyen", taiKhoan.getQuyen())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey())
                .compact();
    }

    public Long extractMaTK(String authorizationHeader) {
        Claims claims = parseClaims(resolveToken(authorizationHeader));
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

        throw new RuntimeException("Token không hợp lệ");
    }

    public boolean hasBearerToken(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7);
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException exception) {
            throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn");
        }
    }

    private String resolveToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new RuntimeException("Thiếu token đăng nhập");
        }

        String token = authorizationHeader.trim();

        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
            token = token.substring(7).trim();
        }

        if (token.isBlank()) {
            throw new RuntimeException("Thiếu token đăng nhập");
        }

        return token;
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
