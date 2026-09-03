package com.tripmoa.security.oauth;

import java.util.Map;

// Naver OAuth2 사용자 정보 파싱 클래스
public class NaverOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    // 네이버는 사용자 정보가 "response" 안에 들어있음
    @SuppressWarnings("unchecked")
    private Map<String, Object> getResponse() {
        return (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getId() {
        return String.valueOf(getResponse().get("id"));  // 네이버 고유 ID
    }

    @Override
    public String getEmail() {
        return (String) getResponse().get("email");
    }

    @Override
    public String getName() {
        return (String) getResponse().get("name");
    }

    @Override
    public String getGender() {
        String gender = (String) getResponse().get("gender"); // 'F' 또는 'M'으로 옴
        if ("F".equals(gender)) return "FEMALE";
        if ("M".equals(gender)) return "MALE";
        return null;
    }

    @Override
    public String getBirthDay() {
        String birthyear = (String) getResponse().get("birthyear"); // YYYY
        String birthday = (String) getResponse().get("birthday");   // MM-DD
        if (birthyear != null && birthday != null) {
            return birthyear + "-" + birthday; // YYYY-MM-DD 형식 완성
        }
        return null;
    }

    @Override
    public String getImageUrl() {
        return (String) getResponse().get("profile_image");
    }
}
