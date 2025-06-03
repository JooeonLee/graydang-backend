package com.graydang.app.global.security;

import java.util.Map;

public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    @SuppressWarnings("unchecked")
    protected Map<String, Object> safeCast(Object obj) {
        return (Map<String, Object>) obj;
    }

    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();
    public abstract String getImageUrl();
} 