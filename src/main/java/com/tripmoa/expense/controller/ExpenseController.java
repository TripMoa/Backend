package com.tripmoa.expense.controller;

import com.tripmoa.expense.dto.request.ExpenseCreateRequest;
import com.tripmoa.expense.dto.request.ExpensePreviewRequest;
import com.tripmoa.expense.dto.response.ExpenseDetailResponse;
import com.tripmoa.expense.dto.response.ExpensePreviewResponse;
import com.tripmoa.expense.dto.response.ExpenseResponse;
import com.tripmoa.expense.service.ExpenseService;
import com.tripmoa.expense.service.SettlementPreviewService;
import com.tripmoa.security.principal.CustomUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Expense", description = "영수증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final SettlementPreviewService settlementPreviewService;

    @GetMapping
    public ResponseEntity<List<ExpenseDetailResponse>> getExpenses(
            @PathVariable Long tripId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(expenseService.getExpenses(tripId, userId));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<ExpenseDetailResponse> getExpense(
            @PathVariable Long tripId,
            @PathVariable Long expenseId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(expenseService.getExpense(tripId, expenseId, userId));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(
            @PathVariable Long tripId,
            @Valid @RequestPart("request") ExpenseCreateRequest request,
            @RequestPart(value = "receiptImage", required = false) MultipartFile receiptImage,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(expenseService.create(tripId, userId, request, receiptImage));
    }

    @PutMapping(value = "/{expenseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExpenseResponse> update(
            @PathVariable Long tripId,
            @PathVariable Long expenseId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestPart("request") ExpenseCreateRequest request,
            @RequestPart(value = "receiptImage", required = false) MultipartFile receiptImage
    ) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(expenseService.update(tripId, expenseId, userId, request, receiptImage));
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