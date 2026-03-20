package com.tripmoa.expense.dto.response;

import lombok.Builder;

@Builder
public record SummarySettlementMemberResponse(
        Long memberId,
        String nickname,
        Integer paidAmount,
        Integer sharedAmount,
        Integer balance
) {
}