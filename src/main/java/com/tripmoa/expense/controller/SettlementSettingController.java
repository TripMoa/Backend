package com.tripmoa.expense.controller;

import com.tripmoa.expense.dto.request.*;
import com.tripmoa.expense.dto.response.SettlementSettingResponse;
import com.tripmoa.expense.dto.response.SettlementSummaryResponse;
import com.tripmoa.expense.service.SettlementSettingService;
import com.tripmoa.expense.service.SettlementSummaryService;
import com.tripmoa.security.princpal.CustomUserDetails;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 정산 설정 API
 * - 조회: Trip 소유주 OR Trip 멤버
 * - 변경: Trip 소유주만
 */

@Tag(name = "Settlement", description = "정산 설정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/settlement-setting")
public class SettlementSettingController {

    private final SettlementSettingService settingService;
    private final SettlementSummaryService settlementSummaryService;

    /**
     * 정산 설정 조회
     * - 권한: Trip 소유주 OR Trip 멤버
     */
    @GetMapping
    public ResponseEntity<SettlementSettingResponse> getSetting(
            @PathVariable Long tripId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(settingService.getSetting(tripId, userId));
    }

    /**
     * 설정 일괄 저장 (설정 적용 버튼)
     * - 권한: Trip 소유주만
     */
    @PutMapping
    public ResponseEntity<SettlementSettingResponse> updateAllSettings(
            @PathVariable Long tripId,
            @Valid @RequestBody SettlementUpdateAllRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(settingService.updateAll(tripId, userId, request));
    }

    /**
     * 설정 금액 미리보기
     * - DB에 저장하지 않고 계산 결과만 반환
     */
    @PostMapping("/preview")
    public ResponseEntity<SettlementSummaryResponse> getPreview(
            @PathVariable Long tripId,
            @Valid @RequestBody SettlementUpdateAllRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(settlementSummaryService.getPreview(tripId, userId, request));
    }

    /**
     * 정산 요약 조회 (Summary)
     * - 권한: Trip 소유주 OR Trip 멤버
     * - 역할 : 현재 정산 설정을 기준으로 저장된 지출을 집계하여 화면용 데이터 반환
     */
    @GetMapping("/summary")
    public ResponseEntity<SettlementSummaryResponse> getSummary(
            @PathVariable Long tripId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(settlementSummaryService.getSummary(tripId, userId));
    }
}