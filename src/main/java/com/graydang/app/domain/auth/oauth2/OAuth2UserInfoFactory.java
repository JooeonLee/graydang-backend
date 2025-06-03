package com.graydang.app.domain.auth.oauth2;

import java.util.Map;

public class OAuth2UserInfoFactory {
    
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        switch (registrationId.toLowerCase()) {
            case "google":
                return new GoogleOAuth2UserInfo(attributes);
            case "kakao":
                return new KakaoOAuth2UserInfo(attributes);
            case "naver":
                return new NaverOAuth2UserInfo(attributes);
            default:
                throw new IllegalArgumentException("죄송합니다! " + registrationId + " 로그인은 아직 지원되지 않습니다.");
        }
    }
}

class GoogleOAuth2UserInfo extends OAuth2UserInfo {
    
    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("picture");
    }
}

class KakaoOAuth2UserInfo extends OAuth2UserInfo {
    
    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getName() {
        Map<String, Object> properties = safeCast(attributes.get("properties"));
        if (properties == null) return null;
        return (String) properties.get("nickname");
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = safeCast(attributes.get("kakao_account"));
        if (kakaoAccount == null) return null;
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getImageUrl() {
        Map<String, Object> properties = safeCast(attributes.get("properties"));
        if (properties == null) return null;
        return (String) properties.get("profile_image");
    }
}

class NaverOAuth2UserInfo extends OAuth2UserInfo {
    
    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        Map<String, Object> response = safeCast(attributes.get("response"));
        if (response == null) return null;
        return (String) response.get("id");
    }

    @Override
    public String getName() {
        Map<String, Object> response = safeCast(attributes.get("response"));
        if (response == null) return null;
        return (String) response.get("name");
    }

    @Override
    public String getEmail() {
        Map<String, Object> response = safeCast(attributes.get("response"));
        if (response == null) return null;
        return (String) response.get("email");
    }

    @Override
    public String getImageUrl() {
        Map<String, Object> response = safeCast(attributes.get("response"));
        if (response == null) return null;
        return (String) response.get("profile_image");
    }
} 