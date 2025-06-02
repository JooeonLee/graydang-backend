package com.graydang.app.global.security;

import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.model.UserCredential;
import com.graydang.app.domain.user.model.UserProfile;
import com.graydang.app.domain.user.repository.UserRepository;
import com.graydang.app.domain.user.repository.UserCredentialRepository;
import com.graydang.app.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        log.info("OAuth2 Login - Provider: {}, Attributes: {}", registrationId, attributes);
        
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);
        
        User user = findOrCreateUser(userInfo, registrationId);
        
        return new CustomUserDetails(user, attributes);
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
                .build();
        
        User savedUser = userRepository.save(user);
        
        UserProfile userProfile = UserProfile.builder()
                .profileImage(userInfo.getImageUrl())
                .nickname(userInfo.getName() != null ? userInfo.getName() : "User")
                .keyword1("정치")
                .keyword2("경제")
                .keyword3("사회")
                .keyword4("문화")
                .keyword5("스포츠")
                .status("ACTIVE")
                .user(savedUser)
                .build();
        
        userProfileRepository.save(userProfile);
        
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