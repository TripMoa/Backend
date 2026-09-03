package com.tripmoa.schedule.controller;

import com.tripmoa.schedule.dto.*;
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

    // 노드추가
    // POST /api/schedule-items
    @PostMapping
    public ResponseEntity<ScheduleItemResponse> create(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @RequestBody ScheduleItemCreateRequest request) {

        return ResponseEntity.ok(scheduleItemService.create(request));
    }

    // 노드 수정
    // PATCH /api/schedule-items/{itemId}
    @PatchMapping("/{itemId}")
    public ResponseEntity<ScheduleItemResponse> update(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @PathVariable Long itemId,
                                                       @RequestBody ScheduleItemUpdateRequest request) {

        return ResponseEntity.ok(scheduleItemService.update(itemId, request));
    }

    // 노드 삭제
    // DELETE /api/schedule-items/{itemId}
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @PathVariable Long itemId) {

        scheduleItemService.delete(itemId);
        return ResponseEntity.noContent().build();
    }

    // 순서 변경
    // PATCH /api/schedule-items/reorder
    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorder(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @RequestBody ScheduleItemReorderRequest request) {

        scheduleItemService.reorder(request);
        return ResponseEntity.ok().build();
    }

    // 다른 날로 이동
    // PATCH /api/schedule-items/{itemId}/move
    @PatchMapping("/{itemId}/move")
    public ResponseEntity<ScheduleItemResponse> move(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     @PathVariable Long itemId,
                                                     @RequestBody ScheduleItemMoveRequest request) {

        return ResponseEntity.ok(scheduleItemService.move(itemId, request));
    }

}