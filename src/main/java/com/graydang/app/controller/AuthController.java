package com.graydang.app.controller;

import com.graydang.app.domain.user.model.User;
import com.graydang.app.global.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/success")
    public ResponseEntity<Map<String, Object>> loginSuccess(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("OAuth2 login successful for user: {}", userDetails.getUsername());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Login successful");
        response.put("user", createUserResponse(userDetails.getUser()));
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/failure")
    public ResponseEntity<Map<String, Object>> loginFailure() {
        log.error("OAuth2 login failed");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Login failed");
        
        return ResponseEntity.badRequest().body(response);
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