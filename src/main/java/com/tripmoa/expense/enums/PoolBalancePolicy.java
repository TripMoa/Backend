package com.tripmoa.expense.enums;

// 남은 돈 처리 정책
public enum PoolBalancePolicy {
    EQUAL,                  // 전체 멤버 균등 분배
    BY_DEPOSIT_RATIO,       // 입금 비율 기준 분배
    CARRY_OVER              // 다음달로 이월
}
