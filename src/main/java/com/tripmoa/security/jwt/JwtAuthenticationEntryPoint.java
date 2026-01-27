package com.tripmoa.security.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 인증되지 않은 사용자가 보호된 API에 접근했을 때 401 Unauthorized 응답을 JSON 형태로 내려주는 클래스

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // 인증 실패 시 자동 호출되는 메서드
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        // HTTP 상태 코드 401 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // JSON 응답 형식 지정
        response.setContentType("application/json;charset=UTF-8");

        // 프론트에서 읽기 쉬운 에러 메시지
        response.getWriter().write("""
            {
              "status": 401,
              "message": "인증이 필요합니다. 다시 로그인해주세요."
            }
        """);
    }
}

