package com.tripmoa.user.controller;

import com.tripmoa.security.princpal.CustomUserDetails;
import com.tripmoa.user.dto.UserResponseDto;
import com.tripmoa.user.dto.UserUpdateRequestDto;
import com.tripmoa.user.service.AuthService;
import com.tripmoa.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// 로그인한 사용자의 내 정보 조회 / 수정 / 탈퇴 API를 담당하는 컨트롤러

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    // 로그아웃 Post
    @PostMapping("/logout")
    public void logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            authService.logout(userDetails.getUser().getId());
        }
    }

    // 내 정보 조회 Get
    @GetMapping("/users/me")
    public UserResponseDto me(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userService.getMyInfo(userDetails.getUser().getId());
    }

    // 전체 프로필 수정 Patch
    @PatchMapping("/users/me") // 경로를 /nickname에서 /me로 변경 제안
    public void updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UserUpdateRequestDto request) {
        userService.updateUserInfo(userDetails.getUser().getId(), request);
    }

    // 회원 탈퇴 Delete
    @DeleteMapping("/users/me")
    public void withdraw(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.withdraw(userDetails.getUser().getId());
    }

}
