package com.tripmoa.expense.dto.request;

// LLM 요청 데이터
public record OcrLlmRequest (
    String storeName,
    String menuName,
    String paymentMethod,
    String dateTime,
    Integer totalAmount
) {}
