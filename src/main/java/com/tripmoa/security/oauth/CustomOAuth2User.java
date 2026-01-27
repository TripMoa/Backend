package com.tripmoa.security.oauth;

import com.tripmoa.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

/**
 * 소셜 로그인 사용자 정보를 우리 서비스 User 엔티티와 함께 감싸는 클래스
 * Spring Security의 SecurityContext에 저장되는 객체
 */

@Getter
public class CustomOAuth2User implements OAuth2User {

    // 우리 서비스의 User 엔티티 (DB에 저장된 실제 사용자)
    private final User user;

    // 구글/카카오에서 받은 원본 사용자 정보
    private final Map<String, Object> attributes;

    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    // 사용자 권한 목록
    // -> 지금은 ROLE_USER 같은 권한을 쓰지 않아서 null 처리
    // -> 나중에 권한 시스템 만들면 여기에 추가 가능
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    // Spring Security가 사용하는 사용자 식별자
    // -> 보통 "username" 역할을 함
    // -> 우리는 User의 id를 문자열로 반환
    @Override
    public String getName() {
        return user.getId().toString();
    }

    // OAuth2User 인터페이스에서 요구하는 메서드
    // -> 구글/카카오에서 받은 원본 데이터 반환
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}

