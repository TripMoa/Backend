package com.tripmoa.schedule.controller;

import com.tripmoa.schedule.dto.*;
import com.tripmoa.schedule.service.ScheduleComputeService;
import com.tripmoa.schedule.service.ScheduleItemService;
import com.tripmoa.security.principal.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedule-items")
@RequiredArgsConstructor
public class ScheduleItemController {

    private final ScheduleItemService scheduleItemService;
    private final ScheduleComputeService scheduleComputeService;

    // 노드추가
    // POST /api/schedule-items
    @PostMapping
    public ResponseEntity<ScheduleItemResponse> create(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @RequestBody ScheduleItemCreateRequest request) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(scheduleItemService.create(userId, request));
    }

    // 구간 실시간 대중교통 경로 조회 (ODsay) — 결과는 저장하지 않고 캐시도 막는다
    // POST /api/schedule-items/transit
    @PostMapping("/transit")
    public ResponseEntity<java.util.Map<String, Object>> transit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ScheduleItemTransitRequest request) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .body(scheduleItemService.transit(userId, request));
    }

    // 노드 수정
    // PATCH /api/schedule-items/{itemId}
    @PatchMapping("/{itemId}")
    public ResponseEntity<ScheduleItemResponse> update(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @PathVariable Long itemId,
                                                       @RequestBody ScheduleItemUpdateRequest request) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(scheduleItemService.update(userId, itemId, request));
    }

    // 노드 삭제
    // DELETE /api/schedule-items/{itemId}
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @PathVariable Long itemId) {
        Long userId = userDetails.getUser().getId();
        scheduleItemService.delete(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    // 순서 변경
    // PATCH /api/schedule-items/reorder
    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorder(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @RequestBody ScheduleItemReorderRequest request) {
        Long userId = userDetails.getUser().getId();
        scheduleItemService.reorder(userId, request);
        return ResponseEntity.ok().build();
    }

    // 머무는 시간·고정 시각 변경 — 자동 계산이 켜진 날이면 그날 시각을 다시 계산해 그 일차를 돌려준다
    // PATCH /api/schedule-items/{itemId}/plan
    @PatchMapping("/{itemId}/plan")
    public ResponseEntity<ScheduleResponse> updatePlan(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @PathVariable Long itemId,
                                                       @RequestBody ScheduleItemPlanRequest request) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(scheduleComputeService.updatePlan(userId, itemId, request));
    }

    // 메모 저장 (멤버 공용)
    // PATCH /api/schedule-items/{itemId}/memo
    @PatchMapping("/{itemId}/memo")
    public ResponseEntity<ScheduleItemResponse> updateMemo(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                           @PathVariable Long itemId,
                                                           @RequestBody ScheduleItemMemoRequest request) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(scheduleItemService.updateMemo(userId, itemId, request));
    }

    // 장소 바꾸기 — 순서·머무는 시간은 그대로, 장소만 교체. 그 일차를 돌려준다
    // PATCH /api/schedule-items/{itemId}/place
    @PatchMapping("/{itemId}/place")
    public ResponseEntity<ScheduleResponse> replacePlace(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                         @PathVariable Long itemId,
                                                         @RequestBody ScheduleItemPlaceRequest request) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(scheduleComputeService.replacePlace(userId, itemId, request));
    }

    // 다른 날로 이동
    // PATCH /api/schedule-items/{itemId}/move
    @PatchMapping("/{itemId}/move")
    public ResponseEntity<ScheduleItemResponse> move(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @PathVariable Long itemId,
                                                     @RequestBody ScheduleItemMoveRequest request) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(scheduleItemService.move(userId, itemId, request));
    }

}
