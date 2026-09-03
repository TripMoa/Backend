package com.tripmoa.expense.dto.response;

import com.tripmoa.expense.entity.ExpenseSplit;

public record ExpenseDetailSplitResponse(
        Long memberId,
        String nickname,
        Integer amount,
        boolean payer
) {
    public static ExpenseDetailSplitResponse from(ExpenseSplit split, Long payerMemberId) {
        return new ExpenseDetailSplitResponse(
                split.getMember().getId(),
                split.getMember().getNickname(),
                split.getAmount(),
                split.getMember().getId().equals(payerMemberId)
        );
    }
}