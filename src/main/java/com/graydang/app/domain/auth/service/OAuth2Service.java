package com.graydang.app.domain.auth.service;

import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.model.UserCredential;
import com.graydang.app.domain.user.repository.UserRepository;
import com.graydang.app.domain.user.repository.UserCredentialRepository;
import com.graydang.app.domain.user.repository.UserProfileRepository;
import com.graydang.app.domain.auth.oauth2.CustomUserDetails;
import com.graydang.app.domain.auth.oauth2.OAuth2UserInfo;
import com.graydang.app.domain.auth.oauth2.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserProfileRepository userProfileRepository;
    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;

//    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
//    private String naverClientId;
//
//    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
//    private String naverClientSecret;

    @Transactional
    public Authentication processOAuth2Login(String provider, String authorizationCode, String redirectUri) {
        try {
            String accessToken = exchangeCodeForToken(provider, authorizationCode, redirectUri);
            
            Map<String, Object> userAttributes = getUserInfo(provider, accessToken);
            log.info("User attributes: {}", userAttributes);
            
            OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(provider, userAttributes);
            
            User user = findOrCreateUser(userInfo, provider);
            
            CustomUserDetails userDetails = new CustomUserDetails(user, userAttributes);
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
        } catch (Exception e) {
            log.error("OAuth2 login failed for provider: {}", provider, e);
            throw new RuntimeException("OAuth2 login failed", e);
        }
    }

    private String exchangeCodeForToken(String provider, String authorizationCode, String redirectUri) {
        String tokenUrl;
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        
        switch (provider.toLowerCase()) {
            case "google":
                tokenUrl = "https://oauth2.googleapis.com/token";
                params.add("client_id", googleClientId);
                params.add("client_secret", googleClientSecret);
                params.add("code", authorizationCode);
                params.add("grant_type", "authorization_code");
                params.add("redirect_uri", redirectUri);
                break;

            case "kakao":
                tokenUrl = "https://kauth.kakao.com/oauth/token";
                params.add("client_id", kakaoClientId);         // 아래에서 @Value 추가해야 함
                params.add("client_secret", kakaoClientSecret); // 아래에서 @Value 추가해야 함
                params.add("code", authorizationCode);
                params.add("grant_type", "authorization_code");
                params.add("redirect_uri", redirectUri);
                break;
                
//            case "naver":
//                tokenUrl = "https://nid.naver.com/oauth2.0/token";
//                params.add("client_id", naverClientId);
//                params.add("client_secret", naverClientSecret);
//                params.add("code", authorizationCode);
//                params.add("grant_type", "authorization_code");
//                params.add("state", "STATE_STRING");
//                break;
                
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        
        @SuppressWarnings("rawtypes")
				ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            @SuppressWarnings("rawtypes")
            Map responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Response body is null");
            }
            String accessToken = (String) responseBody.get("access_token");
            if (accessToken == null) {
                throw new RuntimeException("access_token is null");
            }
            return accessToken;
        }
        
        throw new RuntimeException("Failed to exchange authorization code for token");
    }

    private Map<String, Object> getUserInfo(String provider, String accessToken) {
        String userInfoUrl;
        
        switch (provider.toLowerCase()) {
            case "google":
                //userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
                userInfoUrl = "https://openidconnect.googleapis.com/v1/userinfo";
                break;
//            case "naver":
//                userInfoUrl = "https://openapi.naver.com/v1/nid/me";
//                break;
            case "kakao":
                userInfoUrl = "https://kapi.kakao.com/v2/user/me";
                break;
            default:
                throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<String> request = new HttpEntity<>(headers);
        
        @SuppressWarnings("rawtypes")
				ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            return body;
        }
        
        throw new RuntimeException("Failed to get user info");
    }

    private User findOrCreateUser(OAuth2UserInfo userInfo, String provider) {
        Optional<User> existingUser = userRepository.findByProviderAndProviderUserId(provider, userInfo.getId());
        
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        String username = generateUsername(userInfo.getEmail(), userInfo.getName());
        Optional<User> userByUsername = userRepository.findByUsername(username);
        
        if (userByUsername.isPresent()) {
            User existingUserEntity = userByUsername.get();
            addCredentialToUser(existingUserEntity, provider, userInfo.getId());
            return existingUserEntity;
        }
        
        return createNewUser(userInfo, provider, username);
    }

    private User createNewUser(OAuth2UserInfo userInfo, String provider, String username) {
        User user = User.builder()
                .username(username)
                .role("USER")
                .status("ACTIVE")
                .build();
        
        User savedUser = userRepository.save(user);
        
//        UserProfile userProfile = UserProfile.builder()
//                .profileImage(userInfo.getImageUrl())
//                .nickname(userInfo.getName() != null ? userInfo.getName() : "User")
//                .keyword1("정치")
//                .keyword2("경제")
//                .keyword3("사회")
//                .keyword4("문화")
//                .keyword5("스포츠")
//                .status("ACTIVE")
//                .user(savedUser)
//                .build();
//
//        userProfileRepository.save(userProfile);
        
        addCredentialToUser(savedUser, provider, userInfo.getId());
        
        return userRepository.findById(savedUser.getId()).orElse(savedUser);
    }
    
    private void addCredentialToUser(User user, String provider, String providerUserId) {
        UserCredential credential = UserCredential.builder()
                .provider(provider)
                .providerUserId(providerUserId)
                .status("ACTIVE")
                .user(user)
                .build();
        
        userCredentialRepository.save(credential);
    }
    
    private String generateUsername(String email, String name) {
        if (email != null && !email.isEmpty()) {
            String baseUsername = email.split("@")[0];
            return ensureUniqueUsername(baseUsername);
        }
        
        if (name != null && !name.isEmpty()) {
            String baseUsername = name.replaceAll("\\s+", "").toLowerCase();
            return ensureUniqueUsername(baseUsername);
        }
        
        return ensureUniqueUsername("user");
    }
    
    private String ensureUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
} 