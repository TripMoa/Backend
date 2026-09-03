package com.tripmoa.user.controller;

import com.tripmoa.global.config.CookieUtil;
import com.tripmoa.global.exception.BusinessException;
import com.tripmoa.global.exception.ErrorCode;
import com.tripmoa.security.principal.CustomUserDetails;
import com.tripmoa.user.dto.*;
import com.tripmoa.user.service.AuthService;
import com.tripmoa.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// 로그인한 사용자의 내 정보 조회 / 수정 / 탈퇴 API를 담당하는 컨트롤러

@Tag(name = "User", description = "사용자 정보 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final CookieUtil cookieUtil;

    // 로그아웃 Post
    @PostMapping("/logout")
    public void logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response
    ) {
        if (userDetails != null) {
            authService.logout(userDetails.getUser().getId());
        }

        ResponseCookie deleteCookie = cookieUtil.deleteRefreshCookie();

        response.addHeader("Set-Cookie", deleteCookie.toString());
    }

    // 내 정보 조회 Get
    @GetMapping("/users/me")
    public UserResponseDto me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userService.getMyInfo(userDetails.getUser().getId());
    }

    // 가입 확인 Post
    @PostMapping("/users/check-email")
    public CheckEmailResponse checkEmail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CheckEmailRequest request
    ) {
        if (userDetails == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        return userService.checkEmail(request.getEmail());
    }

    // 전체 프로필 수정 Patch
    @PatchMapping("/users/me") // 경로를 /nickname에서 /me로 변경 제안
    public void updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UserUpdateRequestDto request) {
        userService.updateUserInfo(userDetails.getUser().getId(), request);
    }

    // 성인 인증 Patch
    @PatchMapping("/users/me/age-verification")
    public AgeVerificationResponseDto verifyAdult(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return userService.verifyAdult(userDetails.getUser().getId());
    }

    // 회원 탈퇴 Delete
    @DeleteMapping("/users/me")
    public void withdraw(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.withdraw(userDetails.getUser().getId());
    }

}
