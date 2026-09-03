package com.tripmoa.expense.dto.response;

// wrapper 응답

public record OcrAutofillWithPreviewResponse(
        OcrAutofillResponse autofill,       // autofill = OCR 결과
        ExpensePreviewResponse preview      // preview = 첫 계산 결과
) {
}