package com.tripmoa.security.oauth;

import java.util.Map;

// Google OAuth2 사용자 정보 파싱 클래스
class GoogleOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("sub"));  // 구글 고유 ID
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getGender() {
        String gender = (String) attributes.get("gender"); // 구글 설정에 따라 다를 수 있음
        if (gender == null) return null;

        gender = gender.toUpperCase();
        if (gender.contains("FEMALE")) return "FEMALE";
        if (gender.contains("MALE")) return "MALE";
        return null;
    }

    @Override
    public String getBirthDay() {
        // 구글은 생년월일을 제공하기 위해 별도의 People API 권한이 필요할 수 있습니다.
        // 기본 profile 스코프만 사용하는 경우 null이 반환될 확률이 높습니다.
        return (String) attributes.get("birthday"); // YYYY-MM-DD 형식 기대
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("picture");
    }
}
