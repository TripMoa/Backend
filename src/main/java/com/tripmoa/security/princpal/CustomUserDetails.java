package com.tripmoa.security.princpal;

import com.tripmoa.user.entity.User;
import com.tripmoa.user.enums.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security에서 사용하는 "인증된 사용자 정보" 표현 클래스
 * 우리 서비스의 User 엔티티를 감싸서 SecurityContext에 저장되는 객체
 */

@Getter
public class CustomUserDetails implements UserDetails {

    // 우리 서비스의 실제 사용자 엔티티
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // 사용자 권한 목록
    // -> 지금은 ROLE_USER 하나만 사용
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_USER");
    }

    // Security에서 사용하는 사용자 이름 (우리는 email 대신 userId를 문자열로 사용)
    @Override
    public String getUsername() {
        return user.getId().toString();
    }

    // 비밀번호 (소셜 로그인이라 사용 안 함)
    @Override
    public String getPassword() {
        return null;
    }

    // 계정 만료 여부
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠김 여부
    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatus.SUSPENDED;
    }

    // 비밀번호 만료 여부
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 여부 -> WITHDRAWN(탈퇴)면 false
    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE;
    }
}

