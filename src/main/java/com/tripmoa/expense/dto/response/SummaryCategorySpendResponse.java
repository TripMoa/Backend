package com.tripmoa.expense.dto.response;

import com.tripmoa.expense.enums.ExpenseCategory;
import lombok.Builder;

@Builder
public record SummaryCategorySpendResponse(
        ExpenseCategory category,
        Integer amount
) {
}