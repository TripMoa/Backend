package com.tripmoa.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

// JWT 토큰 생성 / 검증 / 정보 추출 클래스

@Component
public class JwtTokenProvider {

    // 설정한 비밀키
    @Value("${jwt.secret}")
    private String secretKey;

    // 토큰 유효 시간 (1시간) -> 1000ms * 60초 * 60분
    private final long ACCESS_TOKEN_VALID_TIME = 1000L * 60 * 60;

    // 리프레시 토큰 시간 (14일)
    private final long REFRESH_TOKEN_VALID_TIME = 1000L * 60 * 60 * 24 * 14;

    // SecretKey를 JWT 서명용 Key 객체로 변환
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 액세스 토큰 생성
    public String createAccessToken(Long userId) {
        return createToken(userId, ACCESS_TOKEN_VALID_TIME);
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(Long userId) {
        return createToken(userId, REFRESH_TOKEN_VALID_TIME);
    }

    /**
     * 공통 토큰 생성
     * @param userId 로그인한 사용자 ID
     * @return JWT 문자열
     */
    private String createToken(Long userId, long validTime) {

        // 토큰 안에 담을 정보 (payload)
        Claims claims = Jwts.claims();
        claims.put("userId", userId);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + validTime);

        // JWT 생성
        return Jwts.builder()
                .setClaims(claims)          // 사용자 정보
                .setIssuedAt(now)           // 발급 시간
                .setExpiration(expiry)      // 만료 시간
                .signWith(getSigningKey())  // 서명 (위변조 방지)
                .compact();
    }

    // 토큰에서 userId 추출
    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    // 토큰 유효성 검사 (위조 여부, 만료 여부)
    public boolean validateToken(String token) {
        try {
            parseClaims(token); // 파싱 성공 = 유효한 토큰
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 토큰을 파싱해서 Claims(내용) 반환
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

