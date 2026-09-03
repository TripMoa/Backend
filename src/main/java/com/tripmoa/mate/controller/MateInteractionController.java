package com.tripmoa.mate.controller;

import com.tripmoa.mate.dto.LikeResponse;
import com.tripmoa.mate.service.MateLikeService;
import com.tripmoa.mate.service.PassedPostService;
import com.tripmoa.security.principal.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mate")
public class MateInteractionController {

    private final MateLikeService likeService;
    private final PassedPostService passedPostService;

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

    // passed 게시글
    @PostMapping("/posts/{postId}/pass")
    public ResponseEntity<Void> pass(@PathVariable Long postId,
                                     @AuthenticationPrincipal CustomUserDetails user) {
        passedPostService.pass(user.getUser().getId(), postId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/{postId}/pass")
    public ResponseEntity<Void> unpass(@PathVariable Long postId,
                                       @AuthenticationPrincipal CustomUserDetails user) {
        passedPostService.unpass(user.getUser().getId(), postId);
        return ResponseEntity.ok().build();
    }
}
