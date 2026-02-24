package com.tripmoa.community.mate.controller;

import com.tripmoa.community.mate.domain.MatePost;
import com.tripmoa.community.mate.dto.*;
import com.tripmoa.community.mate.service.MateApplicationService;
import com.tripmoa.community.mate.service.MateLikeService;
import com.tripmoa.community.mate.service.MateService;
import com.tripmoa.security.princpal.CustomUserDetails;
import com.tripmoa.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
//import org.springframework.security.access.prepost.PreAuthorize;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mate")
public class MateController {
    private final MateService mateService;
    private final MateApplicationService applyService;
    private final MateLikeService likeService;

    // 전체 메이트 포스트 조회
    @GetMapping("/")
    public ResponseEntity<List<MateResponse>> getMatePosts() {
        List<MateResponse> matePosts = this.mateService.getMatePosts();
        return ResponseEntity.ok().body(matePosts);
    }

    // 메이트 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<MateResponse> getMatePostDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
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

    // 내가 받은 메이트 신청서 조회
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/applications/received")
    public ResponseEntity<List<ApplicationResponse>> getReceivedApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        List<ApplicationResponse> applications = this.applyService.getReceivedApplication(userId);
        return ResponseEntity.ok().body(applications);
    }

    // 내가 보낸 메이트 신청서 조회
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/applications/sent")
    public ResponseEntity<List<ApplicationResponse>> getSentApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        List<ApplicationResponse> applications = this.applyService.getSentApplication(userId);
        return ResponseEntity.ok().body(applications);
    }


    // 특정 메이트 신청
    @PostMapping("/{id}/apply/applicant")
    public ResponseEntity<ApplicationResponse> applicateMatePost(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            ApplicationRequest request
    ) {
        User applicant = userDetails.getUser();
        ApplicationResponse apply = this.applyService.createApply(id, request, applicant);
        return ResponseEntity.ok().body(apply);
    }

    // 메이트 신청 승인
    @PutMapping("/applications/{applyId}/approve")
    public ResponseEntity<?> approve(
            @PathVariable Long applyId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User author = userDetails.getUser();
        ApplicationResponse approveApply = this.applyService.approveApply(applyId, author);
        return ResponseEntity.ok().body(approveApply);
    }

    // 메이트 신청 거절
    @PutMapping("/applications/{applyId}/reject")
    public ResponseEntity<?> reject(
            @PathVariable Long applyId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User author = userDetails.getUser();
        ApplicationResponse rejectApply = this.applyService.rejectApply(applyId, author);
        return ResponseEntity.ok().body(rejectApply);
    }

    // 좋아요 조회
    @GetMapping("/{postId}/like")
    public ResponseEntity<Long> getLikeCount(@PathVariable Long postId) {
        Long likeCount = likeService.getLikeCount(postId);
        return ResponseEntity.ok().body(likeCount);
    }

    // 좋아요 토글
    @PostMapping("/{postId}/like")
    public ResponseEntity<LikeResponse> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        LikeResponse response = likeService.toggleLike(postId, userId);
        return ResponseEntity.ok().body(response);
    }

}
