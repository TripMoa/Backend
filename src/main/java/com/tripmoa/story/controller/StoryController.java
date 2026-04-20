package com.tripmoa.story.controller;

import com.tripmoa.story.dto.post.StoryCreateRequest;
import com.tripmoa.story.dto.post.StoryResponse;
import com.tripmoa.story.dto.post.StoryUpdateRequest;
import com.tripmoa.story.service.StoryService;
import com.tripmoa.story.service.StoryLikeService;
import com.tripmoa.security.principal.CustomUserDetails;
import com.tripmoa.story.service.SavedItineraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;
    private final StoryLikeService storyLikeService;
    private final SavedItineraryService savedItineraryService;

    // 전체 여행기 목록 (GET /api/stories)
    @GetMapping
    public ResponseEntity<List<StoryResponse>> getAllStories(
            @RequestParam(required = false) String tag,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUser().getId() : null;
        return ResponseEntity.ok(storyService.getStorys(userId, tag));
    }

    // 내가 작성한 여행기 목록 (GET /api/stories/my)
    @GetMapping("/my")
    public ResponseEntity<List<StoryResponse>> getMyStories(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(storyService.getStorysByAuthor(userId));
    }

    // 여행기 상세 조회 (GET /api/stories/{id})
    @GetMapping("/{id}")
    public ResponseEntity<StoryResponse> getStory(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails != null ? userDetails.getUser().getId() : null;
        return ResponseEntity.ok(storyService.getStory(id, userId));
    }

    // 여행기 작성 (POST /api/stories)
    @PostMapping
    public ResponseEntity<StoryResponse> createStory(
            @RequestBody StoryCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        StoryResponse created = storyService.createStory(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 여행기 수정 (PATCH /api/stories/{id})
    @PatchMapping("/{id}")
    public ResponseEntity<StoryResponse> updateStory(
            @PathVariable Long id,
            @RequestBody StoryUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        StoryResponse updated = storyService.updateStory(id, request, userId);
        return ResponseEntity.ok(updated);
    }

    // 여행기 삭제 (DELETE /api/stories/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStory(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        storyService.deleteStory(id, userId);
        return ResponseEntity.noContent().build();
    }

    // 조회수 증가 (POST /api/stories/{id}/view)
    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementViews(@PathVariable Long id) {
        storyService.incrementViews(id);
        return ResponseEntity.ok().build();
    }

    // 좋아요 토글 (POST /api/stories/{id}/like)
    @PostMapping("/{id}/like")
    public ResponseEntity<Boolean> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        boolean liked = storyLikeService.toggleLike(id, userId);
        return ResponseEntity.ok(liked);
    }

    // 좋아요한 여행기 목록 (GET /api/stories/liked)
    @GetMapping("/liked")
    public ResponseEntity<List<StoryResponse>> getLikedStories(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(storyService.getLikedStories(userId));
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
    public ResponseEntity<List<StoryResponse>> getSavedItineraries(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(savedItineraryService.getSavedItineraries(userId));
    }
}