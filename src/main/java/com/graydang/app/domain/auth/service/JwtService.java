package com.graydang.app.domain.auth.service;

import com.graydang.app.domain.auth.exception.InvalidTokenException;
import com.graydang.app.domain.auth.util.JwtUtil;
import com.graydang.app.global.common.model.enums.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    private static final String REFRESH_PREFIX = "refresh:user:";
    private static final String BLACKLIST_PREFIX = "blacklisted:";


    public Map<String, String> issueToken(Long userId, String username, String role, String provider, String providerId) {
        String accessToken = jwtUtil.createAccessToken(userId, username, role, provider, providerId);
        String refreshToken = jwtUtil.createRefreshToken(userId, username, role, provider, providerId);

        saveRefreshToken(userId, refreshToken);

        return Map.of("accessToken", accessToken,
                "refreshToken", refreshToken);
    }

    public Map<String, String> reissueTokens(String oldRefreshToken) {
        validateRefreshTokenOrThrow(oldRefreshToken);

        Long userId = jwtUtil.getUserId(oldRefreshToken);
        String username = jwtUtil.getUsername(oldRefreshToken);
        String role = jwtUtil.getRole(oldRefreshToken);
        String provider = jwtUtil.getProvider(oldRefreshToken);
        String providerId = jwtUtil.getProviderId(oldRefreshToken);

        addToBlackList(oldRefreshToken);

        String newAccessToken = jwtUtil.createAccessToken(userId, username, role, provider, providerId);
        String newRefreshToken = jwtUtil.createRefreshToken(userId, username, role, provider, providerId);

        saveRefreshToken(userId, newRefreshToken);

        return Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken
        );
    }

    public void saveRefreshToken(Long userId, String refreshToken) {
        long ttl = jwtUtil.getExpiration(refreshToken);
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + userId,
                refreshToken,
                ttl,
                TimeUnit.SECONDS
        );
    }

    public void addToBlackList(String token) {
        long ttl = jwtUtil.getExpiration(token);
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "logout",
                ttl,
                TimeUnit.SECONDS
        );
    }

    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey(BLACKLIST_PREFIX + token);
    }

    private void validateRefreshTokenOrThrow(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new InvalidTokenException(BaseResponseStatus.INVALID_TOKEN);
        }

        if (!jwtUtil.isRefreshToken(token)) {
            throw new InvalidTokenException(BaseResponseStatus.EXPIRED_TOKEN);
        }

        if (isBlacklisted(token)) {
            throw new InvalidTokenException(BaseResponseStatus.BLACKLISTED_REFRESH_TOKEN);
        }
    }
}
