package com.graydang.app.domain.auth.controller;

import com.graydang.app.domain.auth.dto.LoginRequestDto;
import com.graydang.app.domain.auth.dto.LoginResponseDto;
import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.domain.user.repository.UserProfileRepository;
import com.graydang.app.global.common.model.dto.BaseResponse;
import com.graydang.app.global.security.jwt.JwtTokenProvider;
import com.graydang.app.domain.auth.service.OAuth2Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth-Controller", description = "OAuth2.0 인증 및 JWT 관련 API 엔드포인트")
public class AuthController {

    private final OAuth2Service oAuth2Service;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserProfileRepository userProfileRepository;

    @PostMapping("/oauth/{provider}")
    @Operation(summary = "소셜 로그인", description = "Authorization Code로 소셜 로그인 처리 및 JWT 토큰 발급")
    public BaseResponse<LoginResponseDto> oauthLogin(
            @Parameter(description = "소셜 로그인 제공자 (e.g. kakao, google)", example = "kakao")
            @PathVariable
            String provider,
            @Valid @RequestBody LoginRequestDto request) {
            
        Authentication authentication = oAuth2Service.processOAuth2Login(provider, request.getCode(), request.getRedirectUri());

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);
            
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        boolean onboarded = userProfileRepository.findByUserIdAndStatus(userId, "ACTIVE").isPresent();

        LoginResponseDto responseDto = new LoginResponseDto(accessToken, refreshToken, userId, onboarded);

        log.info("OAuth2 login successful for user: {} via provider: {}", userDetails.getUsername(), provider);
            
        return new BaseResponse<>(responseDto);
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "JWT 재발급", description = "Refresh 토큰을 사용해 새로운 Access 토큰 발급")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            
            if (refreshToken == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "리프레시 토큰이 필요합니다"
                ));
            }
            
            if (!jwtTokenProvider.validateToken(refreshToken) || !jwtTokenProvider.isRefreshToken(refreshToken)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "유효하지 않은 리프레시 토큰입니다"
                ));
            }
            
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            
            Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                username, null, java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
            );
            
            String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accessToken", newAccessToken);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "토큰 갱신 실패");
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", false);
            return ResponseEntity.ok(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", true);
        response.put("user", createUserResponse(userDetails.getUser()));
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "프론트엔드에서 토큰 삭제만 하면 됨")
    public ResponseEntity<Map<String, Object>> logout() {
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "로그아웃 성공");
        
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("username", user.getUsername());
        userResponse.put("role", user.getRole());
        
        if (user.getProfile() != null) {
            Map<String, Object> profile = new HashMap<>();
            profile.put("nickname", user.getProfile().getNickname());
            profile.put("profileImage", user.getProfile().getProfileImage());
            profile.put("keywords", new String[]{
                user.getProfile().getKeyword1(),
                user.getProfile().getKeyword2(),
                user.getProfile().getKeyword3(),
                user.getProfile().getKeyword4(),
                user.getProfile().getKeyword5()
            });
            userResponse.put("profile", profile);
        }
        
        return userResponse;
    }
} 