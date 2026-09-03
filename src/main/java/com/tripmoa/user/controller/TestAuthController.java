package com.tripmoa.user.controller;

import com.tripmoa.user.entity.User;
import com.tripmoa.security.jwt.JwtTokenProvider;
import com.tripmoa.user.enums.UserStatus;
import com.tripmoa.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "TestAuth", description = "테스트용 JWT Token 발급 API")
@RestController
@RequestMapping("/api/test/auth")
@RequiredArgsConstructor
@Profile({"local", "dev"}) // 운영에서는 막기
public class TestAuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * 테스트용 JWT 발급 API
     * - userId로 access / refresh token 발급
     * - Swagger 테스트용
     */
    @PostMapping("/token")
    public ResponseEntity<?> issueTestToken(@RequestBody TestTokenRequest request) {

        if (request.getUserId() == null) {
            return ResponseEntity.badRequest().body("userId는 필수입니다.");
        }

        // 유저 존재 확인
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("유저 없음: userId=" + request.getUserId()));

        //  상태 체크
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("ACTIVE 유저만 토큰 발급 가능");
        }

        // 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("grantType", "Bearer");
        response.put("userId", user.getId());
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);

        return ResponseEntity.ok(response);
    }

    /**
     * 요청 DTO
     */
    public static class TestTokenRequest {
        private Long userId;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }
}