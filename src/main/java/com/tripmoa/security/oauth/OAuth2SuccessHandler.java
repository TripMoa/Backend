package com.tripmoa.security.oauth;

import com.tripmoa.security.jwt.JwtTokenProvider;
import com.tripmoa.user.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 소셜 로그인 성공 시 실행되는 핸들러
 *
 * 1. CustomOAuth2User에서 우리 User 엔티티 꺼내기
 * 2. JWT 토큰 생성
 * 3. 프론트엔드(React)로 토큰 전달
 */

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        // 현재 로그인한 소셜 공급자(Provider) 이름 가져오기
        String provider = user.getSocialAccounts().stream()
                .filter(sa -> Boolean.TRUE.equals(sa.getConnected()))
                .map(sa -> sa.getProvider().name())
                .findFirst()
                .orElse("GOOGLE"); // 기본값

        // 액세스 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(user.getId());

        // 리프레시 토큰 생성 및 DB 저장
        String refreshToken = authService.createAndSaveRefreshToken(user);

        // 프론트엔드로 두 토큰 모두 전달
        String redirectUrl = String.format(
                "http://localhost:5173/oauth2/redirect?token=%s&refreshToken=%s&provider=%s",
                accessToken,
                refreshToken,
                provider
        );

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        /**
         * 방법 B: HTTPOnly 쿠키로 전달 (실서비스 권장)
         *
         * Cookie cookie = new Cookie("accessToken", token);
         * cookie.setHttpOnly(true);
         * cookie.setSecure(true);
         * cookie.setPath("/");
         * cookie.setMaxAge(60 * 60); // 1시간
         * response.addCookie(cookie);
         * response.sendRedirect("http://localhost:3000");
         */
    }
}
