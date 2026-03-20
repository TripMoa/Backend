package com.tripmoa.expense.enums;

// 나머지 처리 정책
public enum SplitRemainderPolicy {
    TO_PAYER,       // 결제자 할당
    ROUND_ROBIN,    // 순서대로 할당
    RANDOM          // 랜덤 할당
}
