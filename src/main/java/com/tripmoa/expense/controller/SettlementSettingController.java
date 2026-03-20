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
     * payment mode 변경 (토글 즉시 저장)
     * - 권한: Trip 소유주만
     */
    @PutMapping("/payment-mode")
    public ResponseEntity<SettlementSettingResponse> changePaymentMode(
            @PathVariable Long tripId,
            @Valid @RequestBody PaymentModeChangeRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(settingService.changePaymentMode(tripId, userId, request));
    }

    /**
     * split remainder policy 변경 (토글 즉시 저장)
     * - 권한: Trip 소유주만
     * - 정책: 모든 mode에서 변경 가능
     * - 설명: 기존 지출 원본은 변경하지 않고, 이후 preview / 저장부터 적용
     */
    @PutMapping("/split-remainder-policy")
    public ResponseEntity<SettlementSettingResponse> changeSplitRemainderPolicy(
            @PathVariable Long tripId,
            @Valid @RequestBody SplitRemainderPolicyChangeRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(settingService.changeSplitRemainderPolicy(tripId, userId, request));
    }

    /**
     * pool balance policy 변경 (토글 즉시 저장)
     * - 권한: Trip 소유주만
     * - 정책: 모든 mode에서 변경 가능
     * - 설명: 현재 mode에서 필요할 때만 summary 계산에 사용
     */
    @PutMapping("/pool-balance-policy")
    public ResponseEntity<SettlementSettingResponse> changePoolBalancePolicy(
            @PathVariable Long tripId,
            @Valid @RequestBody PoolBalancePolicyChangeRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(settingService.changePoolBalancePolicy(tripId, userId, request));
    }

    /**
     * budget 변경 (적용 버튼 저장)
     * - 권한: Trip 소유주만
     */
    @PutMapping("/budget")
    public ResponseEntity<SettlementSettingResponse> updateBudget(
            @PathVariable Long tripId,
            @Valid @RequestBody BudgetAmountUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(settingService.updateBudget(tripId, userId, request));
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