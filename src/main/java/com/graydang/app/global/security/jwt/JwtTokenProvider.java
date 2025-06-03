package com.graydang.app.global.security.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final String secret;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.secret:mySecretKeyForJWT}") String secret,
            @Value("${jwt.access-token-validity:3600000}") long accessTokenValidityInMilliseconds,
            @Value("${jwt.refresh-token-validity:604800000}") long refreshTokenValidityInMilliseconds) {
        this.secret = secret;
        this.accessTokenValidityInMilliseconds = accessTokenValidityInMilliseconds;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds;
    }

    public String createAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Instant now = Instant.now();
        Instant validity = now.plus(accessTokenValidityInMilliseconds, ChronoUnit.MILLIS);

        return createSimpleJWT(authentication.getName(), authorities, "access", now, validity);
    }

    public String createRefreshToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant validity = now.plus(refreshTokenValidityInMilliseconds, ChronoUnit.MILLIS);

        return createSimpleJWT(authentication.getName(), "", "refresh", now, validity);
    }

    private String createSimpleJWT(String subject, String authorities, String type, Instant issuedAt, Instant expiration) {
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        
        String payload = String.format(
                "{\"sub\":\"%s\",\"auth\":\"%s\",\"type\":\"%s\",\"iat\":%d,\"exp\":%d}",
                subject, authorities, type, issuedAt.getEpochSecond(), expiration.getEpochSecond()
        );
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        
        String signature = createSignature(header + "." + encodedPayload);
        
        return header + "." + encodedPayload + "." + signature;
    }

    private String createSignature(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error creating JWT signature", e);
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }
            
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            String subjectPattern = "\"sub\":\"([^\"]+)\"";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(subjectPattern);
            java.util.regex.Matcher matcher = pattern.matcher(payload);
            
            if (matcher.find()) {
                return matcher.group(1);
            }
            throw new IllegalArgumentException("Subject not found in token");
        } catch (Exception e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            
            String expectedSignature = createSignature(parts[0] + "." + parts[1]);
            if (!expectedSignature.equals(parts[2])) {
                return false;
            }
            
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            String expPattern = "\"exp\":(\\d+)";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(expPattern);
            java.util.regex.Matcher matcher = pattern.matcher(payload);
            
            if (matcher.find()) {
                long exp = Long.parseLong(matcher.group(1));
                return Instant.now().getEpochSecond() < exp;
            }
            
            return false;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return payload.contains("\"type\":\"refresh\"");
        } catch (Exception e) {
            return false;
        }
    }
} 