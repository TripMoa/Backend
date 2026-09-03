package com.tripmoa.expense.enums;

// 정산 방식
public enum PaymentMode {
    INDEPENDENT,    // 결제 후 정산
    POOL,           // 모임 통장
    HYBRID          // 통합 모드
}