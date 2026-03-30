package com.tripmoa.expense.controller;

import com.tripmoa.expense.dto.request.DepositLogCreateRequest;
import com.tripmoa.expense.dto.response.DepositLogResponse;
import com.tripmoa.expense.service.DepositLogService;
import com.tripmoa.security.princpal.CustomUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Deposit", description = "입금 로그 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/deposits")
public class DepositLogController {

    private final DepositLogService depositLogService;

    /**
     * 입금 로그 저장
     * - 권한: Trip 소유주 OR Trip 멤버
     */
    @PostMapping
    public ResponseEntity<DepositLogResponse> create(
            @PathVariable Long tripId,
            @Valid @RequestBody DepositLogCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(depositLogService.create(tripId, userId, request));
    }

    /**
     * 입금 로그 조회
     * - 권한: Trip 소유주 OR Trip 멤버
     */
    @GetMapping("/members/{memberId}")
    public ResponseEntity<List<DepositLogResponse>> getMemberDepositLogs(
            @PathVariable Long tripId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(depositLogService.getMemberDepositLogs(tripId, memberId, userId));
    }

    /**
     * 입금 로그 삭제
     * - 권한: Trip 소유주 OR 입금 로그 등록자 OR 실제 입금한 대상 멤버
     */
    @DeleteMapping("/{depositLogId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long tripId,
            @PathVariable Long depositLogId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        depositLogService.delete(tripId, depositLogId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 입금 로그 승인
     * - 권한: Trip 소유주만
     */
    @PatchMapping("/{depositLogId}/confirm")
    public ResponseEntity<DepositLogResponse> confirm(
            @PathVariable Long tripId,
            @PathVariable Long depositLogId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(depositLogService.confirm(tripId, depositLogId, userId));
    }

    /**
     * 입금 로그 승인 거절
     * - 권한: Trip 소유주만
     */
    @PatchMapping("/{depositLogId}/reject")
    public ResponseEntity<DepositLogResponse> reject(
            @PathVariable Long tripId,
            @PathVariable Long depositLogId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(depositLogService.reject(tripId, depositLogId, userId));
    }
}