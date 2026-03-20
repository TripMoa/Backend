package com.tripmoa.expense.dto.request;

import com.tripmoa.expense.enums.PoolBalancePolicy;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 통장(POOL) 남은 돈 처리 정책 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
public class PoolBalancePolicyChangeRequest {

    // 남은 돈 처리 정책 (EQUAL / BY_DEPOSIT_RATIO / CARRY_OVER)
    @NotNull(message = "poolBalancePolicy는 필수입니다.")
    private PoolBalancePolicy poolBalancePolicy;
}