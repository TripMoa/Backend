package com.tripmoa.user.controller;

import com.tripmoa.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        // 토큰이 없는 경우 처리
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("리프레시 토큰이 누락되었습니다.");
        }

        try {
            // AuthService의 로직 호출 (DB 검증 + JWT 검증 + 토큰 로테이션)
            Map<String, String> tokens = authService.refreshAccessToken(refreshToken);

            // 성공 시 새로운 Access/Refresh 토큰 반환
            return ResponseEntity.ok(tokens);

        } catch (RuntimeException e) {
            // DB에 없거나 만료된 경우 401 응답 -> 프론트가 이를 가로채서 로그아웃시킴
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}