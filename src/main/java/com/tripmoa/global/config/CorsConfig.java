package com.tripmoa.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.*;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CorsConfig
 *
 * - CORS 전역 설정 클래스
 * - 프론트엔드(React/Vite)에서 API 서버 접근 허용
 * - 허용 도메인: localhost:3000, 5173
 * - 모든 HTTP 메서드 및 헤더 허용
 * - Authorization 헤더 노출
 * - 자격 증명(Credentials) 포함 요청 허용
 */

@Configuration
public class CorsConfig {

    /**
     *  CORS 설정
     * - 프론트엔드(React, Next.js 등)에서 API 서버에 접근할 수 있도록 허용하는 설정입니다.
     * - React 개발 서버: http://localhost:3000 (또는 Vite 5173)
     * - 배포 도메인 생기면 allowedOrigins에 추가
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 개발용 : 접근을 허용할 프론트엔드 도메인 (주소들)
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));

        // 허용할 HTTP 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // 모든 헤더 허용
        config.setAllowedHeaders(List.of("*"));
        // 프론트엔드에서 Authorization 헤더를 읽을 수 있도록 노출 (필요하면 토큰 헤더 노출)
        config.setExposedHeaders(List.of("Authorization"));
        // 쿠키 및 인증 정보를 포함한 요청 허용
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 모든 API 경로(/**)에 대해 위 설정을 적용
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}