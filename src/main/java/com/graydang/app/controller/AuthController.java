package com.graydang.app.controller;

import com.graydang.app.domain.user.model.User;
import com.graydang.app.global.security.CustomUserDetails;
import com.graydang.app.global.security.jwt.JwtTokenProvider;
import com.graydang.app.global.security.oauth2.OAuth2Service;
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
public class AuthController {

    private final OAuth2Service oAuth2Service;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/oauth/{provider}")
    public ResponseEntity<Map<String, Object>> oauthLogin(
            @PathVariable String provider,
            @RequestBody Map<String, String> request) {
        
        try {
            String authorizationCode = request.get("code");
            String redirectUri = request.get("redirectUri");
            
            if (authorizationCode == null || redirectUri == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "인증 코드와 리다이렉트 URI가 필요합니다"
                ));
            }
            
            Authentication authentication = oAuth2Service.processOAuth2Login(provider, authorizationCode, redirectUri);
            
            String accessToken = jwtTokenProvider.createAccessToken(authentication);
            String refreshToken = jwtTokenProvider.createRefreshToken(authentication);
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "로그인 성공");
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("user", createUserResponse(userDetails.getUser()));
            
            log.info("OAuth2 login successful for user: {} via provider: {}", userDetails.getUsername(), provider);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("OAuth2 login failed for provider: {}", provider, e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "로그인 실패: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/refresh")
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