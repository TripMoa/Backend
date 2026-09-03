package com.tripmoa.expense.dto.response;

import com.tripmoa.expense.entity.Expense;
import com.tripmoa.expense.enums.ExpenseCategory;
import com.tripmoa.expense.enums.PayMethod;
import com.tripmoa.expense.enums.SplitMode;

import java.time.LocalDateTime;
import java.util.List;

public record ExpenseDetailResponse(

        Long id,
        Long tripId,
        Long payerMemberId,
        String payerNickname,

        String storeName,
        String itemMemo,

        Integer totalAmount,
        ExpenseCategory category,
        PayMethod payMethod,

        Boolean isShared,
        Boolean autoIncludePayer,
        SplitMode splitMode,

        String receiptUrl,
        String receiptFileName,

        LocalDateTime paidAt,
        LocalDateTime createdAt,

        List<ExpenseDetailSplitResponse> splits
) {
    public static ExpenseDetailResponse from(Expense expense) {
        Long payerMemberId = expense.getPayerMember().getId();

        List<ExpenseDetailSplitResponse> splitResponses = expense.getSplits().stream()
                .map(split -> ExpenseDetailSplitResponse.from(split, payerMemberId))
                .toList();

        return new ExpenseDetailResponse(
                expense.getId(),
                expense.getTrip().getId(),
                payerMemberId,
                expense.getPayerMember().getNickname(),
                expense.getStoreName(),
                expense.getItemMemo(),
                expense.getTotalAmount(),
                expense.getCategory(),
                expense.getPayMethod(),
                expense.isShared(),
                expense.isAutoIncludePayer(),
                expense.getSplitMode(),
                expense.getReceiptUrl(),
                expense.getReceiptFileName(),
                expense.getPaidAt(),
                expense.getCreatedAt(),
                splitResponses
        );
    }
}