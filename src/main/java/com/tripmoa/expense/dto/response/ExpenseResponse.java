package com.tripmoa.expense.dto.response;

import com.tripmoa.expense.entity.Expense;
import com.tripmoa.expense.enums.ExpenseCategory;
import com.tripmoa.expense.enums.PayMethod;
import com.tripmoa.expense.enums.SplitMode;

import java.time.LocalDateTime;

public record ExpenseResponse(

        Long id,
        Long tripId,
        Long payerMemberId,

        String storeName,
        String itemMemo,

        Integer totalAmount,
        ExpenseCategory category,
        PayMethod payMethod,

        Boolean isShared,
        SplitMode splitMode,

        LocalDateTime paidAt,
        LocalDateTime createdAt

) {
    public static ExpenseResponse from(Expense e) {
        return new ExpenseResponse(
                e.getId(),
                e.getTrip().getId(),
                e.getPayerMember().getId(),
                e.getStoreName(),
                e.getItemMemo(),
                e.getTotalAmount(),
                e.getCategory(),
                e.getPayMethod(),
                e.isShared(),
                e.getSplitMode(),
                e.getPaidAt(),
                e.getCreatedAt()
        );
    }
}