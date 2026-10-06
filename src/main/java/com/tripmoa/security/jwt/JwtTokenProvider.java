package com.tripmoa.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.JwtException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

// JWT 토큰 생성 / 검증 / 정보 추출 클래스

@Component
public class JwtTokenProvider {

    // access/refresh 토큰 구분용 타입
    // "type" 클레임이 없는 토큰(이번 변경 이전에 발급된 토큰)은 legacy 취급 →
    // getTokenType()이 ACCESS로 기본값 처리 (isAccessToken 쪽만 관대하게 허용, isRefreshToken은 엄격)
    public enum TokenType {
        ACCESS, REFRESH
    }

    // TODO : 설정한 비밀키 고정 (개발용)
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
        return createToken(userId, ACCESS_TOKEN_VALID_TIME, TokenType.ACCESS);
    }

    // 리프레시 토큰 생성
    public String createRefreshToken(Long userId) {
        return createToken(userId, REFRESH_TOKEN_VALID_TIME, TokenType.REFRESH);
    }

    /**
     * 공통 토큰 생성
     * @param userId 로그인한 사용자 ID
     * @return JWT 문자열
     */
    private String createToken(Long userId, long validTime, TokenType type) {

        // 토큰 안에 담을 정보 (payload)
        Claims claims = Jwts.claims();
        claims.put("userId", userId);
        claims.put("type", type.name());

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

    // 토큰에서 타입 추출 ("type" 클레임이 없는 구버전 토큰은 ACCESS로 취급)
    public TokenType getTokenType(String token) {
        String type = parseClaims(token).get("type", String.class);
        return type != null ? TokenType.valueOf(type) : TokenType.ACCESS;
    }

    // 리소스 API 접근용으로 쓸 수 있는 토큰인지 (액세스 토큰, 혹은 구버전 무타입 토큰)
    public boolean isAccessToken(String token) {
        return getTokenType(token) == TokenType.ACCESS;
    }

    // 토큰 재발급(/api/auth/refresh)에 쓸 수 있는 리프레시 토큰인지 (엄격 — 구버전 무타입 토큰은 불허)
    public boolean isRefreshToken(String token) {
        return getTokenType(token) == TokenType.REFRESH;
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

