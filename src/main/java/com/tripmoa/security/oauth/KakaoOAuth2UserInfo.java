package com.tripmoa.security.oauth;

import java.util.Map;

// Kakao OAuth2 사용자 정보 파싱 클래스
public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getEmail() {
        Map<String, Object> account =
                (Map<String, Object>) attributes.get("kakao_account");

        return (String) account.get("email");
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getName() {
        Map<String, Object> profile =
                (Map<String, Object>) ((Map<String, Object>) attributes
                        .get("kakao_account")).get("profile");

        return (String) profile.get("nickname");
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getGender() {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        String gender = (String) account.get("gender"); // 'male', 'female'로 옴

        if ("female".equals(gender)) return "FEMALE";
        if ("male".equals(gender)) return "MALE";
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getBirthDay() {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        String birthyear = (String) account.get("birthyear"); // YYYY
        String birthday = (String) account.get("birthday");   // MMDD

        if (birthyear != null && birthday != null) {
            // MMDD 형식을 MM-DD로 변환하여 YYYY-MM-DD 완성
            return birthyear + "-" + birthday.substring(0, 2) + "-" + birthday.substring(2);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getImageUrl() {
        Map<String, Object> profile =
                (Map<String, Object>) ((Map<String, Object>) attributes
                        .get("kakao_account")).get("profile");

        return (String) profile.get("profile_image_url");
    }
}

