package com.tripmoa.notice.controller;

import com.tripmoa.security.principal.CustomUserDetails;
import com.tripmoa.notice.dto.request.NoticeGroupCreateRequest;
import com.tripmoa.notice.dto.request.NoticeGroupRenameRequest;
import com.tripmoa.notice.dto.response.NoticeGroupResponse;
import com.tripmoa.notice.service.NoticeGroupService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "NoticeGroup", description = "공지사항 그룹 관리 API")
@RestController
@RequestMapping("/api/trips/{tripId}/notice-groups")
@RequiredArgsConstructor
public class NoticeGroupController {

    private final NoticeGroupService noticeGroupService;

    /**
     * 공지 그룹 전체 조회
     */
    @GetMapping
    public ResponseEntity<List<NoticeGroupResponse>> getNoticeGroups(
            @PathVariable Long tripId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(noticeGroupService.getNoticeGroups(tripId, userId));
    }

    /**
     * 공지 그룹 단건 조회
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<NoticeGroupResponse> getNoticeGroup(
            @PathVariable Long tripId,
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(noticeGroupService.getNoticeGroup(tripId, groupId, userId));
    }

    /**
     * 공지 그룹 생성
     */
    @PostMapping
    public ResponseEntity<NoticeGroupResponse> createNoticeGroup(
            @PathVariable Long tripId,
            @Valid @RequestBody NoticeGroupCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(noticeGroupService.createNoticeGroup(tripId, userId, request));
    }

    /**
     * 공지 그룹 이름 수정
     */
    @PatchMapping("/{groupId}/name")
    public ResponseEntity<NoticeGroupResponse> renameNoticeGroup(
            @PathVariable Long tripId,
            @PathVariable Long groupId,
            @Valid @RequestBody NoticeGroupRenameRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(noticeGroupService.renameNoticeGroup(tripId, groupId, userId, request));
    }

    /**
     * 공지 그룹 삭제
     */
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteNoticeGroup(
            @PathVariable Long tripId,
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        noticeGroupService.deleteNoticeGroup(tripId, groupId, userId);
        return ResponseEntity.noContent().build();
    }
}