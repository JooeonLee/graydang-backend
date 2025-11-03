package com.graydang.app.common.fixture;

import com.graydang.app.domain.user.model.User;
import com.graydang.app.domain.user.model.UserCredential;
import com.graydang.app.domain.user.model.UserProfile;
import com.graydang.app.domain.user.model.enums.UserStatus;

import java.util.ArrayList;
import java.util.List;

public class UserTestDataBuilder {
    
    public static User createActiveUser() {
        return User.builder()
                .username("testuser")
                .email("test@example.com")
                .role("ROLE_USER")
                .status(UserStatus.ACTIVE)
                .credentials(new ArrayList<>())
                .build();
    }
    
    public static User createActiveUserWithId(Long id) {
        return User.builder()
                .id(id)
                .username("testuser" + id)
                .email("test" + id + "@example.com")
                .role("ROLE_USER")
                .status(UserStatus.ACTIVE)
                .credentials(new ArrayList<>())
                .build();
    }
    
    public static User createBlockedUser() {
        return User.builder()
                .username("blockeduser")
                .email("blocked@example.com")
                .role("ROLE_USER")
                .status(UserStatus.BLOCKED)
                .credentials(new ArrayList<>())
                .build();
    }
    
    public static User createInactiveUser() {
        return User.builder()
                .username("inactiveuser")
                .email("inactive@example.com")
                .role("ROLE_USER")
                .status(UserStatus.INACTIVE)
                .credentials(new ArrayList<>())
                .build();
    }
    
    public static User createUserWithProfile() {
        User user = createActiveUser();
        UserProfile profile = UserProfile.builder()
                .nickname("테스트유저")
                .keyword1("정치")
                .keyword2("경제")
                .status(UserStatus.ACTIVE)
                .user(user)
                .build();
        user.setProfile(profile);
        return user;
    }
    
    public static User createUserWithCredentials() {
        User user = createActiveUser();
        
        UserCredential googleCredential = UserCredential.builder()
                .provider("google")
                .providerUserId("google123")
                .status(UserStatus.ACTIVE)
                .user(user)
                .build();
        
        UserCredential kakaoCredential = UserCredential.builder()
                .provider("kakao")
                .providerUserId("kakao123")
                .status(UserStatus.ACTIVE)
                .user(user)
                .build();
        
        user.getCredentials().add(googleCredential);
        user.getCredentials().add(kakaoCredential);
        
        return user;
    }
    
    public static User createFullUser() {
        User user = createActiveUser();
        
        // UserProfile 추가
        UserProfile profile = UserProfile.builder()
                .nickname("테스트유저")
                .profileImage("https://example.com/profile.jpg")
                .keyword1("정치")
                .keyword2("경제")
                .keyword3("사회")
                .status(UserStatus.ACTIVE)
                .user(user)
                .build();
        user.setProfile(profile);
        
        // UserCredentials 추가
        UserCredential googleCredential = UserCredential.builder()
                .provider("google")
                .providerUserId("google123")
                .status(UserStatus.ACTIVE)
                .user(user)
                .build();
        
        UserCredential kakaoCredential = UserCredential.builder()
                .provider("kakao")
                .providerUserId("kakao123")
                .status(UserStatus.ACTIVE)
                .user(user)
                .build();
        
        user.getCredentials().add(googleCredential);
        user.getCredentials().add(kakaoCredential);
        
        return user;
    }
    
    public static User createAdminUser() {
        return User.builder()
                .username("admin")
                .email("admin@example.com")
                .role("ROLE_ADMIN")
                .status(UserStatus.ACTIVE)
                .credentials(new ArrayList<>())
                .build();
    }
}