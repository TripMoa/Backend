package com.tripmoa.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${oauth2.redirect-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        // access_denied(취소) 조용히 처리
        if (exception instanceof OAuth2AuthenticationException oauth2Ex) {
            String errorCode = oauth2Ex.getError().getErrorCode();
            if ("access_denied".equals(errorCode)) {
                response.sendRedirect(frontendUrl + "/login");
                return;
            }
        }

        // 그 외 실제 오류만 에러 표시
        response.sendRedirect(frontendUrl + "/login?error=true");
    }
}
