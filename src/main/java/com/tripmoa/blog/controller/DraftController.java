package com.tripmoa.blog.controller;

import com.tripmoa.blog.dto.draft.DraftCreateRequest;
import com.tripmoa.blog.dto.draft.DraftResponse;
import com.tripmoa.blog.dto.draft.DraftUpdateRequest;
import com.tripmoa.blog.service.DraftService;
import com.tripmoa.security.princpal.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drafts")
@RequiredArgsConstructor
public class DraftController {

    private final DraftService draftService;

    // 내 임시저장 목록 (GET /api/drafts)
    @GetMapping
    public ResponseEntity<List<DraftResponse>> getDrafts(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(draftService.getDraftsByAuthor(userId));
    }

    // 임시저장 상세 조회 (GET /api/drafts/{id})
    @GetMapping("/{id}")
    public ResponseEntity<DraftResponse> getDraft(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(draftService.getDraft(id, userId));
    }

    // 임시저장 생성 (POST /api/drafts)
    @PostMapping
    public ResponseEntity<DraftResponse> createDraft(
            @RequestBody DraftCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        DraftResponse created = draftService.createDraft(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 임시저장 수정 (PATCH /api/drafts/{id})
    @PatchMapping("/{id}")
    public ResponseEntity<DraftResponse> updateDraft(
            @PathVariable Long id,
            @RequestBody DraftUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        DraftResponse updated = draftService.updateDraft(id, request, userId);
        return ResponseEntity.ok(updated);
    }

    // 임시저장 삭제 (DELETE /api/drafts/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDraft(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        draftService.deleteDraft(id, userId);
        return ResponseEntity.noContent().build();
    }
}