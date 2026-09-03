package com.tripmoa.expense.dto.response;

import lombok.Builder;

@Builder
public record ExpensePreviewSplitResponse(
        Long memberId,
        String nickname,
        Integer amount,
        boolean payer
) {
}