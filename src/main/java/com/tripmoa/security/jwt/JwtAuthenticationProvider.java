package com.tripmoa.security.jwt;

import com.tripmoa.security.princpal.CustomUserDetails;
import com.tripmoa.security.princpal.CustomUserDetailsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰을 검증하고 인증 객체(Authentication)를 만들어주는 클래스
 * - Filter → Provider → 인증 처리
 * - JWT 인증 로직 분리용 구조 클래스
 */

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationProvider(
            JwtTokenProvider jwtTokenProvider,
            CustomUserDetailsService customUserDetailsService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    // 실제 인증 처리 로직
    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        // Filter에서 전달한 JWT 토큰
        String token = (String) authentication.getCredentials();

        // 토큰 유효성 검사
        if (!jwtTokenProvider.validateToken(token)) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        // 토큰에서 userId 추출
        Long userId = jwtTokenProvider.getUserId(token);

        // DB에서 사용자 조회
        CustomUserDetails userDetails =
                customUserDetailsService.loadUserById(userId);

        // 인증 객체 생성
        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    // 어떤 타입의 Authentication을 처리할지 지정
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

