package com.tripmoa.user.service;

import com.tripmoa.security.jwt.JwtTokenProvider;
import com.tripmoa.user.entity.RefreshToken;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.repository.RefreshTokenRepository;
import com.tripmoa.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    // 로그인 시 리프레시 토큰 저장/갱신
    @Transactional
    public String createAndSaveRefreshToken(User user) {
        // 중첩 방지: 기존 유저의 리프레시 토큰이 있다면 모두 삭제
        refreshTokenRepository.deleteByUser(user);

        String refreshTokenValue = jwtTokenProvider.createRefreshToken(user.getId());
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(14);

        // 새로운 토큰 저장
        refreshTokenRepository.save(new RefreshToken(user, refreshTokenValue, expiryDate));
        return refreshTokenValue;
    }

    // 로그아웃 시 DB 토큰 삭제 로직 추가
    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        refreshTokenRepository.deleteByUser(user);
    }

    // 토큰 재발급 검증 로직
    @Transactional
    public Map<String, String> refreshAccessToken(String refreshToken) {
        // DB에서 해당 토큰 존재 여부 확인
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("DB에 존재하지 않는 리프레시 토큰입니다."));

        // JWT 유효성 및 만료 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            refreshTokenRepository.delete(storedToken);
            throw new RuntimeException("만료된 리프레시 토큰입니다. 다시 로그인하세요.");
        }

        User user = storedToken.getUser();
        String newAccess = jwtTokenProvider.createAccessToken(user.getId());
        String newRefresh = jwtTokenProvider.createRefreshToken(user.getId());

        // DB 토큰 값 업데이트 (Rotation 방식 적용)
        storedToken.updateToken(newRefresh, LocalDateTime.now().plusDays(14));

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccess);
        tokens.put("refreshToken", newRefresh);
        return tokens;
    }
}
