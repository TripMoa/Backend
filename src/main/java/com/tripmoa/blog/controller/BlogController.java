package com.tripmoa.blog.controller;

import com.tripmoa.blog.dto.post.BlogCreateRequest;
import com.tripmoa.blog.dto.post.BlogResponse;
import com.tripmoa.blog.dto.post.BlogUpdateRequest;
import com.tripmoa.blog.service.BlogService;
import com.tripmoa.blog.service.BlogLikeService;
import com.tripmoa.security.princpal.CustomUserDetails;
import com.tripmoa.blog.service.SavedItineraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final BlogLikeService blogLikeService;
    private final SavedItineraryService savedItineraryService;

    // 전체 여행기 목록 (GET /api/stories)
    @GetMapping
    public ResponseEntity<List<BlogResponse>> getAllStories(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUser().getId() : null;
        return ResponseEntity.ok(blogService.getBlogs(userId));
    }

    // 내가 작성한 여행기 목록 (GET /api/stories/my)
    @GetMapping("/my")
    public ResponseEntity<List<BlogResponse>> getMyStories(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(blogService.getBlogsByAuthor(userId));
    }

    // 여행기 상세 조회 (GET /api/stories/{id})
    @GetMapping("/{id}")
    public ResponseEntity<BlogResponse> getStory(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUser().getId() : null;
        return ResponseEntity.ok(blogService.getBlog(id, userId));
    }

    // 여행기 작성 (POST /api/stories)
    @PostMapping
    public ResponseEntity<BlogResponse> createStory(
            @RequestBody BlogCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        BlogResponse created = blogService.createBlog(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 여행기 수정 (PATCH /api/stories/{id})
    @PatchMapping("/{id}")
    public ResponseEntity<BlogResponse> updateStory(
            @PathVariable Long id,
            @RequestBody BlogUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        BlogResponse updated = blogService.updateBlog(id, request, userId);
        return ResponseEntity.ok(updated);
    }

    // 여행기 삭제 (DELETE /api/stories/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStory(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        blogService.deleteBlog(id, userId);
        return ResponseEntity.noContent().build();
    }

    // 조회수 증가 (POST /api/stories/{id}/view)
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementViews(@PathVariable Long id) {
        blogService.incrementViews(id);
        return ResponseEntity.ok().build();
    }

    // 좋아요 토글 (POST /api/stories/{id}/like)
    @PostMapping("/{id}/like")
    public ResponseEntity<Boolean> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        boolean liked = blogLikeService.toggleLike(id, userId);
        return ResponseEntity.ok(liked);
    }

    // 좋아요한 여행기 목록 (GET /api/stories/liked)
    @GetMapping("/liked")
    public ResponseEntity<List<BlogResponse>> getLikedStories(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(blogService.getLikedStories(userId));
    }

    // 일정 저장 (USE THIS ITINERARY → SAVED ITINERARIES에 저장)
    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> saveItinerary(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        savedItineraryService.saveItinerary(id, userId);
        return ResponseEntity.ok().build();
    }

    // 일정 저장 해제 (SAVED ITINERARIES에서 제거)
    @DeleteMapping("/{id}/follow")
    public ResponseEntity<Void> unsaveItinerary(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        savedItineraryService.unsaveItinerary(id, userId);
        return ResponseEntity.ok().build();
    }

    // 저장된 일정 목록 (SAVED ITINERARIES)
    @GetMapping("/followed")
    public ResponseEntity<List<BlogResponse>> getSavedItineraries(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(savedItineraryService.getSavedItineraries(userId));
    }
}