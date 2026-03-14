package com.tripmoa.blog.controller;

import com.tripmoa.blog.dto.comment.CommentCreateRequest;
import com.tripmoa.blog.dto.comment.CommentResponse;
import com.tripmoa.blog.service.BlogCommentService;
import com.tripmoa.security.princpal.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BlogCommentController {

    private final BlogCommentService commentService;

    // 댓글 목록 조회 (GET /api/stories/{storyId}/comments)
    @GetMapping("/stories/{storyId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long storyId) {
        List<CommentResponse> comments = commentService.getComments(storyId);
        return ResponseEntity.ok(comments);
    }

    // 댓글 작성 (POST /api/stories/{storyId}/comments)
    @PostMapping("/stories/{storyId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long storyId,
            @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        CommentResponse created = commentService.createComment(storyId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 댓글 수정 (PATCH /api/comments/{commentId})
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        CommentResponse updated = commentService.updateComment(commentId, request, userId);
        return ResponseEntity.ok(updated);
    }

    // 댓글 삭제 (DELETE /api/comments/{commentId})
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}