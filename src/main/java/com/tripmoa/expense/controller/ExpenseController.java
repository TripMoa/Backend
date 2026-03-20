package com.tripmoa.expense.controller;

import com.tripmoa.expense.dto.request.ExpenseCreateRequest;
import com.tripmoa.expense.dto.request.ExpensePreviewRequest;
import com.tripmoa.expense.dto.response.ExpensePreviewResponse;
import com.tripmoa.expense.dto.response.ExpenseResponse;
import com.tripmoa.expense.service.ExpenseService;
import com.tripmoa.expense.service.SettlementPreviewService;
import com.tripmoa.security.princpal.CustomUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Expense", description = "영수증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final SettlementPreviewService settlementPreviewService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(
            @PathVariable Long tripId,
            @Valid @RequestBody ExpenseCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(expenseService.create(tripId, userId, request));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<ExpenseResponse> update(
            @PathVariable Long tripId,
            @PathVariable Long expenseId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ExpenseCreateRequest req
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(expenseService.update(tripId, expenseId, userId, req));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long tripId,
            @PathVariable Long expenseId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        expenseService.delete(tripId, expenseId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/preview")
    public ResponseEntity<ExpensePreviewResponse> preview(
            @PathVariable Long tripId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ExpensePreviewRequest request
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(settlementPreviewService.preview(tripId, userId, request));
    }
}