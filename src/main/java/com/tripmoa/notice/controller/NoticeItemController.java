package com.tripmoa.notice.controller;

import com.tripmoa.security.principal.CustomUserDetails;
import com.tripmoa.notice.dto.request.NoticeItemCreateRequest;
import com.tripmoa.notice.dto.request.NoticeItemUpdateRequest;
import com.tripmoa.notice.dto.response.NoticeItemResponse;
import com.tripmoa.notice.dto.response.NoticeTagResponse;
import com.tripmoa.notice.service.NoticeItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips/{tripId}/notice-items")
@RequiredArgsConstructor
public class NoticeItemController {

    private final NoticeItemService noticeItemService;

    /**
     * 공지 메모 전체 조회
     * - groupId 기준으로 조회
     */
    @GetMapping
    public ResponseEntity<List<NoticeItemResponse>> getNoticeItems(
            @PathVariable Long tripId,
            @RequestParam Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(noticeItemService.getNoticeItems(tripId, groupId, userId));
    }

    /**
     * 공지 메모 단건 조회
     */
    @GetMapping("/{noticeItemId}")
    public ResponseEntity<NoticeItemResponse> getNoticeItem(
            @PathVariable Long tripId,
            @PathVariable Long noticeItemId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(noticeItemService.getNoticeItem(tripId, noticeItemId, userId));
    }

    /**
     * 공지 메모 생성
     */
    @PostMapping
    public ResponseEntity<NoticeItemResponse> createNoticeItem(
            @PathVariable Long tripId,
            @Valid @RequestBody NoticeItemCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(noticeItemService.createNoticeItem(tripId, userId, request));
    }

    /**
     * 공지 메모 수정
     */
    @PatchMapping("/{noticeItemId}")
    public ResponseEntity<NoticeItemResponse> updateNoticeItem(
            @PathVariable Long tripId,
            @PathVariable Long noticeItemId,
            @Valid @RequestBody NoticeItemUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(noticeItemService.updateNoticeItem(tripId, noticeItemId, userId, request));
    }

    /**
     * 공지 메모 삭제
     */
    @DeleteMapping("/{noticeItemId}")
    public ResponseEntity<Void> deleteNoticeItem(
            @PathVariable Long tripId,
            @PathVariable Long noticeItemId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        noticeItemService.deleteNoticeItem(tripId, noticeItemId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 핀 고정
     * - 모든 멤버 가능
     */
    @PatchMapping("/{noticeItemId}/pin")
    public ResponseEntity<NoticeItemResponse> pinNoticeItem(
            @PathVariable Long tripId,
            @PathVariable Long noticeItemId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(noticeItemService.pinNoticeItem(tripId, noticeItemId, userId));
    }

    /**
     * 핀 해제
     * - 소유주만 가능
     */
    @PatchMapping("/{noticeItemId}/unpin")
    public ResponseEntity<NoticeItemResponse> unpinNoticeItem(
            @PathVariable Long tripId,
            @PathVariable Long noticeItemId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(noticeItemService.unpinNoticeItem(tripId, noticeItemId, userId));
    }

    /**
     * 최근 사용 태그 조회
     */
    @GetMapping("/tags/recent")
    public ResponseEntity<List<NoticeTagResponse>> getRecentNoticeTags(
            @PathVariable Long tripId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(noticeItemService.getRecentNoticeTags(tripId, userId));
    }

    /**
     * 태그 삭제
     * - 소유주만 가능
     */
    @DeleteMapping("/tags/{tagId}")
    public ResponseEntity<Void> deleteNoticeTag(
            @PathVariable Long tripId,
            @PathVariable Long tagId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        noticeItemService.deleteNoticeTag(tripId, tagId, userId);
        return ResponseEntity.noContent().build();
    }
}