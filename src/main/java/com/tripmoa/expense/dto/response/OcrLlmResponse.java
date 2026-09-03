package com.tripmoa.expense.dto.response;

import com.tripmoa.expense.enums.ExpenseCategory;
import com.tripmoa.expense.enums.PayMethod;

// LLM 응답 데이터
public record OcrLlmResponse(
        String storeName,           // 상호명(필요하면 보정)
        String itemMemo,            // item (메모/용도)
        ExpenseCategory category,   // FOOD/TRANS/STAY/SHOP/TICKET/ETC
        PayMethod payMethod,        // CARD/CASH/QR
        String paidAt,              // yyyy-MM-dd HH:mm:ss
        Integer totalAmount         // 정수
) {}