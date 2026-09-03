package com.tripmoa.expense.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ExpensePreviewManualSplitRequest(

        @NotNull
        Long memberId,

        @NotNull
        @PositiveOrZero
        Integer amount
) {
}