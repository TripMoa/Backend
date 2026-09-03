package com.tripmoa.mate.controller;

import com.tripmoa.mate.dto.*;
import com.tripmoa.mate.service.MateApplicationService;
import com.tripmoa.mate.service.MateLikeService;
import com.tripmoa.mate.service.MateService;
import com.tripmoa.mate.service.PassedPostService;
import com.tripmoa.security.principal.CustomUserDetails;
import com.tripmoa.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
//import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mate")
public class MateController {
    private final MateService mateService;

    // 전체 메이트 포스트 조회
    @GetMapping("/")
    public ResponseEntity<List<MateResponse>> getMatePosts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = (userDetails != null) ? userDetails.getUser().getId() : null;
        List<MateResponse> matePosts = this.mateService.getMatePosts(userId);
        return ResponseEntity.ok().body(matePosts);
    }

    // 메이트 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<MateResponse> getMatePostDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = (userDetails != null) ? userDetails.getUser().getId() : null;
        MateResponse matePostDetail = this.mateService.getPostsById(id, userId);
        return ResponseEntity.ok().body(matePostDetail);
    }

    // 메이트 작성
    @PostMapping("/")
    public ResponseEntity<MateResponse> createMatePost(
            @RequestBody MateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        MateResponse post = this.mateService.createPost(request, user);
        return ResponseEntity.ok().body(post);
    }

    // 메이트 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMatePost(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();
        this.mateService.deletePostById(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/posts/passed")
    public ResponseEntity<List<MateResponse>> getPassed(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(mateService.getPassedPosts(user.getUser().getId()));
    }

    @GetMapping("/posts/expired")
    public ResponseEntity<List<MateResponse>> getExpired(Pageable pageable) {
        return ResponseEntity.ok(mateService.getExpiredPosts(pageable));
    }

}
