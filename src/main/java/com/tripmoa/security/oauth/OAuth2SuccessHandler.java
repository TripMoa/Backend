package com.tripmoa.security.oauth;

import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.security.jwt.JwtTokenProvider;
import com.tripmoa.user.entity.User;
import com.tripmoa.user.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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

    @Value("${oauth2.redirect-url}")
    private String redirectBaseUrl;

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    /**
     * TODO : HTTPOnly 쿠키로 전달 (실서비스 권장)
     * refresh token만 쿠키로 전달.
     * access token은 localStorage에서 관리.
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();

        // 현재 로그인한 소셜 공급자(Provider) 이름 가져오기
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauthToken.getAuthorizedClientRegistrationId().toUpperCase();

        // 리프레시 토큰 생성 및 DB 저장
        String refreshToken;

        try {
            refreshToken = authService.createAndSaveRefreshToken(user);
        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.ACCOUNT_SUSPENDED) {
                getRedirectStrategy().sendRedirect(
                        request,
                        response,
                        redirectBaseUrl + "/login?error=SUSPENDED"
                );
                return;
            }

            getRedirectStrategy().sendRedirect(
                    request,
                    response,
                    redirectBaseUrl + "/login?error=true"
            );
            return;
        }

        ResponseCookie refreshCookie = cookieUtil.createRefreshCookie(refreshToken);

        response.addHeader("Set-Cookie", refreshCookie.toString());

        // provider만 전달
        String redirectUrl = redirectBaseUrl + "/oauth2/redirect?provider=" + provider;

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);

    }
}
