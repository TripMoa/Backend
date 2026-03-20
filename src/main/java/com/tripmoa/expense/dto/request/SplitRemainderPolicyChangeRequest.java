package com.tripmoa.expense.dto.request;

import com.tripmoa.expense.enums.SplitRemainderPolicy;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지출 1/N 나머지 처리 정책 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
public class SplitRemainderPolicyChangeRequest {

    // 나머지 처리 정책 (TO_PAYER / ROUND_ROBIN / RANDOM)
    @NotNull(message = "splitRemainderPolicy는 필수입니다.")
    private SplitRemainderPolicy splitRemainderPolicy;
}