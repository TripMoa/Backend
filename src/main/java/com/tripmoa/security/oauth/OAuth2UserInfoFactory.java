package com.tripmoa.security.oauth;

import java.util.Map;

// OAuth2 로그인 제공자에 따라 알맞은 UserInfo 객체를 생성하는 팩토리 클래스

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(
            String registrationId,
            Map<String, Object> attributes
    ) {

        if (registrationId.equals("google")) {
            return new GoogleOAuth2UserInfo(attributes);
        }

        if (registrationId.equals("kakao")) {
            return new KakaoOAuth2UserInfo(attributes);
        }

        if (registrationId.equals("naver")) {
            return new NaverOAuth2UserInfo(attributes);
        }

        throw new IllegalArgumentException("지원하지 않는 소셜 로그인: " + registrationId);
    }
}

