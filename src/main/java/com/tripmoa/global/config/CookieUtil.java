package com.tripmoa.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    private static final String REFRESH_TOKEN = "refreshToken";
    private static final long MAX_AGE_SECONDS = 60L * 60 * 24 * 14; // 14일

    @Value("${app.cookie.secure:false}")
    private boolean secure;

    @Value("${app.cookie.same-site:Lax}")
    private String sameSite;

    /** 리프레시 토큰 발급용 쿠키 */
    public ResponseCookie createRefreshCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN, refreshToken)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite(sameSite)
                .maxAge(MAX_AGE_SECONDS)
                .build();
    }

    /** 로그아웃/탈퇴 시 쿠키 제거용 */
    public ResponseCookie deleteRefreshCookie() {
        return ResponseCookie.from(REFRESH_TOKEN, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite(sameSite)
                .maxAge(0)
                .build();
    }
}