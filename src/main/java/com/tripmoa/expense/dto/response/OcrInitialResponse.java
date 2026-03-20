package com.tripmoa.expense.dto.response;

// Ocr 초기 응답 데이터
public record OcrInitialResponse(
        String storeName,           // 상호명
        String menuName,            // 메뉴 내역
        String paymentMethod,       // 결제 방식
        String dateTime,            // 결제 일시
        Integer totalAmount         // 총 결제 금액
) {}
