package com.tripmoa.expense.dto.response;

import lombok.Builder;

@Builder
public record SettlementTransactionResponse(
        Long fromMemberId,
        String fromNickname,
        Long toMemberId,
        String toNickname,
        int amount
) {}
