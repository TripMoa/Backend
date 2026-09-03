package com.tripmoa.expense.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ExpenseSplitCreateRequest(
        @NotNull
        Long memberId,

        @NotNull
        @Min(0)
        Integer amount
) {}