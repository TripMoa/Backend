package com.tripmoa.expense.dto.response;

import lombok.Builder;

@Builder
public record SummaryDepositStatusResponse(
        Long memberId,
        String nickname,
        Integer targetAmount,
        Integer depositedAmount,
        Integer remainingAmount,
        Integer overpaidAmount,
        String status
) {
}