package com.graydang.app.domain.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private SecretKey secretKey;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidityInMillis;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidityInMillis;

    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    @PostConstruct
    public void init() {
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String createAccessToken(Long userId, String username, String role, String provider, String providerId) {
        return buildToken(userId, username, role, provider, providerId, TOKEN_TYPE_ACCESS, accessTokenValidityInMillis);
    }

    public String createRefreshToken(Long userId, String username, String role, String provider, String providerId) {
        return buildToken(userId, username, role, provider, providerId, TOKEN_TYPE_REFRESH, refreshTokenValidityInMillis);
    }

    private String buildToken(Long userId, String username, String role, String tokenType, String provider, String providerId, long validityInMillis) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(validityInMillis);

        return Jwts.builder()
                .claim("tokenType", tokenType)
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", role)
                .claim("provider", provider)
                .claim("providerId", providerId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(getTokenType(token));
    }

    public String getTokenType(String token) {
        return getClaim(token, "tokenType", String.class);
    }

    public Long getUserId(String token) {
        return getClaim(token, "userId", Long.class);
    }

    public String getUsername(String token) {
        return getClaim(token, "username", String.class);
    }

    public String getRole(String token) {
        return getClaim(token, "role", String.class);
    }

    public String getProvider(String token) {
        return getClaim(token, "provider", String.class);
    }

    public String getProviderId(String token) {
        return getClaim(token, "providerId", String.class);
    }

    public long getExpiration(String token) {
        Date exp = parseClaims(token).getExpiration();
        return (exp.getTime() - System.currentTimeMillis()) / 1000;
    }

    private <T> T getClaim(String token, String key, Class<T> type) {
        return parseClaims(token).get(key, type);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}