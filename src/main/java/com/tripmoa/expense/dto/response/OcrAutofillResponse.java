package com.tripmoa.expense.dto.response;

import com.tripmoa.expense.enums.ExpenseCategory;
import com.tripmoa.expense.enums.PayMethod;

// LLM 연결 전 임시 저장 데이터
public record OcrAutofillResponse(
        String storeName,
        String itemMemo,                  // = menuName
        ExpenseCategory category,         // = ETC 고정
        PayMethod payMethod,              // = 간단 매핑
        String paidAt,                    // = dateTime
        Integer totalAmount
) {}