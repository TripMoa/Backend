package com.tripmoa.security.oauth;

// 공통 인터페이스
public interface OAuth2UserInfo {

    String getId();
    String getEmail();     // 이메일
    String getName();      // 이름(닉네임)
    String getImageUrl();  // 프로필 이미지
    String getGender();
    String getBirthDay();

}
