package com.tripmoa.expense.dto.request;

import com.tripmoa.expense.enums.ExpenseCategory;

public record OcrAiAutofillResult(
        String itemMemo,
        ExpenseCategory category
) {}
