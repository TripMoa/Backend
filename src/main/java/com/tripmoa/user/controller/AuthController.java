package com.tripmoa.user.controller;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.user.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Auth", description = "Refresh Token 재발급 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        // 토큰이 없는 경우 비로그인 상태
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "reason", "UNAUTHENTICATED"
            ));
        }

        try {
            Map<String, String> tokens = authService.refreshAccessToken(refreshToken);

            String newRefreshToken = tokens.get("refreshToken");

            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                    .httpOnly(true)
                    .secure(false) // TODO : 로컬 http 테스트용 false. 배포(https) true
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(60 * 60 * 24 * 14)
                    .build();

            response.addHeader("Set-Cookie", refreshCookie.toString());

            return ResponseEntity.ok(Map.of(
                    "accessToken", tokens.get("accessToken"),
                    "authenticated", true
            ));

        } catch (BusinessException e) {
            // 인증 유지 불가 상황 → Refresh 쿠키 제거 후 상태값과 함께 응답
            ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(0)
                    .build();
            response.addHeader("Set-Cookie", deleteCookie.toString());

            if (e.getErrorCode() == ErrorCode.ACCOUNT_SUSPENDED) {
                return ResponseEntity.ok(Map.of(
                        "authenticated", false,
                        "reason", "SUSPENDED",
                        "message", "신고 정책으로 정지된 계정입니다."
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "reason", "UNAUTHENTICATED"
            ));

        } catch (RuntimeException e) {
            // 토큰 만료/위조 등 일반 인증 실패
            ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(0)
                    .build();
            response.addHeader("Set-Cookie", deleteCookie.toString());

            return ResponseEntity.ok(Map.of(
                    "authenticated", false,
                    "reason", "UNAUTHENTICATED"
            ));

        } catch (Exception e) {
            // DB 장애 등 예상하지 못한 서버 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}