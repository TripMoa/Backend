package com.tripmoa.global.config;

import com.tripmoa.security.jwt.JwtAuthenticationEntryPoint;
import com.tripmoa.security.jwt.JwtAuthenticationFilter;
import com.tripmoa.security.jwt.JwtTokenProvider;
import com.tripmoa.security.oauth.CustomOAuth2UserService;
import com.tripmoa.security.oauth.OAuth2SuccessHandler;
import com.tripmoa.security.princpal.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig
 *
 * - Spring Security 전역 설정 클래스
 * - JWT 기반 인증 처리 (STATELESS 세션 정책)
 * - OAuth2 소셜 로그인(Google, Kakao, Naver) 설정
 * - 인증 없이 접근 가능한 URL 경로 설정
 * - JWT 필터를 UsernamePasswordAuthenticationFilter 이전에 등록
 * - 인증 실패 시 401 JSON 응답 처리
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // JWT 생성/검증 담당 클래스
    private final JwtTokenProvider jwtTokenProvider;

    // 소셜 로그인 사용자 정보를 우리 User 엔티티로 매핑하는 서비스
    private final CustomOAuth2UserService customOAuth2UserService;

    // 소셜 로그인 성공 시 JWT 발급하고 프론트로 전달하는 핸들러
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    // 인증 실패 시 401 JSON 응답 커스텀하고 싶을 때 사용
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    // 실제 사용자(User)를 DB에서 조회하기 위한 서비스
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // CSRF 설정 : 보안 비활성화
                .csrf(csrf -> csrf.disable())

                // CORS 설정 : 다른 도메인/포트의 접근 허용
                .cors(Customizer.withDefaults())

                // 세션 관리 정책: JWT를 사용하므로 세션을 생성하지 않음 (STATELESS)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // URL별 권한 설정
                .authorizeHttpRequests(auth -> auth

                        // OPTIONS 메서드(CORS 정책) : 모두 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 외부 접근 차단
                        .requestMatchers("/api/v1/ocr/**").denyAll()

                        // OCR 프록시: 로그인 필요
                        .requestMatchers(HttpMethod.POST, "/api/trips/*/expenses/ocr").authenticated()

                        // 소셜 로그인 관련 경로 : 인증 없이 접근 가능
                        .requestMatchers("/oauth2/**", "/login/oauth2/**", "/api/auth/**").permitAll()

                        // 비로그인 사용자도 볼 수 있는 데이터 (Public API) -> GET 경로만 허용으로 수정하기
                        .requestMatchers("/api/travelstory/**", "/api/mate/**").permitAll()

                        // Swagger 경로 허용
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // 서버 상태 확인 및 에러 페이지 : 모두 허용
                        .requestMatchers("/actuator/health", "/error", "/favicon.ico").permitAll()

                        // 업로드된 이미지 파일 : 인증 없이 접근 가능
                        .requestMatchers("/uploads/**").permitAll()

                        // 그 외 모든 요청 : 인증(로그인) 필요
                        .anyRequest().authenticated()
                )

                // OAuth2 로그인 설정
                .oauth2Login(oauth -> oauth
                        // 로그인 성공 -> 소셜 로그인 사용자 정보 로딩
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        // 로그인 성공 핸들러 -> JWT 토큰 발행 및 리다이렉트
                        .successHandler(oAuth2SuccessHandler)
                )

                // 인증 실패 (401) JSON 커스텀 응답
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // 기본 로그인 폼 및 HTTP Basic 인증 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        // JWT 인증 필터 추가 : OAuth2 로그인 이후부터는 모든 요청에서 토큰 검증
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

}

