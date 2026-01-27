package com.tripmoa.security.jwt;

import com.tripmoa.security.princpal.CustomUserDetails;
import com.tripmoa.security.princpal.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 모든 요청마다 실행되는 JWT 인증 필터
 * 1. Authorization 헤더에서 JWT 추출
 * 2. 토큰 유효성 검사
 * 3. 토큰에서 userId 추출
 * 4. DB에서 사용자 조회
 * 5. SecurityContext에 인증 정보 저장
 */

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    // SecurityConfig에서 주입받음
    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            CustomUserDetailsService customUserDetailsService
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    // 실제 필터 로직 -> 요청이 들어올 때마다 실행됨
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Authorization 헤더 가져오기
        String header = request.getHeader("Authorization");

        // "Bearer {토큰}" 형식인지 확인
        if (header != null && header.startsWith("Bearer ")) {

            // "Bearer " 이후의 실제 토큰 값만 추출
            String token = header.substring(7);

            // 토큰 유효성 검사
            if (jwtTokenProvider.validateToken(token)) {

                // 토큰에서 userId 추출
                Long userId = jwtTokenProvider.getUserId(token);

                // DB에서 사용자 정보 조회
                CustomUserDetails userDetails =
                        customUserDetailsService.loadUserById(userId);

                // Spring Security 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,                    // 사용자 정보
                                null,                           // 비밀번호 (JWT라 필요 없음)
                                userDetails.getAuthorities()    // 권한 목록
                        );

                // SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}
